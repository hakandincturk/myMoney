# Server-side sessions (replace JWT) — design

Date: 2026-11-11 · Status: decisions taken (section 9); ready for the implementation plan

## 1. Intent

Today a login returns a 24-hour HS256 JWT that the frontend keeps in `localStorage` and sends as
`Authorization: Bearer`. The backend cannot end a token before it expires: a stolen token works
for up to a day and logout only deletes the browser copy. The signing secret sits in
`application.yaml` in plain text.

The Architect wants every login to be a record the backend owns, so that ending a session is a
backend decision that takes effect on the next request.

**What the Architect asked for (their words, 2026-11-11):**
- "JWT çalındığında yapacak bir şey olmuyor ve account yönetimi daha zor" → sessions must be revocable server-side.
- "oturumları görmek ve istediğimden çıkış yapabilmek isterim" → a list of my active sessions, with logout per session.

## 2. Goals, non-goals, anti-goals

**Goals**
1. A session is a database row. Deleting or revoking it logs that device out on its next request.
2. The browser never exposes the session credential to JavaScript (HttpOnly cookie).
3. A screen that lists the user's active sessions (device, browser, IP/country, last activity, "this device") with a logout button per row and "log out all other sessions".
4. Logout really ends the session on the server.
5. The hardcoded JWT secret and the `jjwt` dependency disappear.

**Non-goals**
- Multi-factor auth, password reset, "remember me" checkbox, OAuth/social login.
- A password-change endpoint (none exists). When one is built, it must revoke the user's other sessions; this design only provides the service method for it.
- Admin views of other users' sessions.

**Anti-goals (would be a failure even if it works)**
- Any request path that still accepts a JWT after the cut-over.
- A database write on every API request (the box has 2 vCPU / 3.7 GB).
- A new piece of infrastructure (Redis etc.) to run and back up on the single OVH server.
- Storing the raw session token in the database (a DB dump must not be a list of live logins).

## 3. Constraints (from the project)

- Java 21, Spring Boot 3.5, Spring Security; N-layered (controller → service → repository); DTO per endpoint; MapStruct; Flyway, new migration only (`V7__...`); soft delete convention; error messages Turkish, code English; every query scoped to the authenticated user.
- Prod: nginx → Spring on 8090, `context-path=/api`, behind Cloudflare (UFW admits only Cloudflare ranges, so `CF-Connecting-IP` / `CF-IPCountry` headers can be trusted). Frontend and API share one origin `https://mymoney.hakandincturk.com`.
- Local dev: frontend `http://localhost:5174` (or 5173), backend `http://127.0.0.1:8090` (frontend `.env` `VITE_API_BASE_URL`; code default `http://localhost:8080` in `environment.ts:13` / `baseApi.ts:7`). `CorsConfig.java:50-56` uses `allowedOriginPatterns` + `allowCredentials(true)`; with `CORS_ALLOWED_ORIGINS` unset the `*` pattern echoes any origin. Prod sets `CORS_ALLOWED_ORIGINS=https://mymoney.hakandincturk.com` in `/opt/mymoney/config/.env`.
- `context-path=/api` is set only in the prod profile (`/opt/mymoney/config/application-prod.yaml`); filter and `SecurityConfig` paths (`/auth/login`) are servlet paths, relative to the context path, so they stay as they are.
- `CF-Connecting-IP` / `CF-IPCountry` trust rests on server config, not on code: UFW admits 80/443 only from Cloudflare ranges (mymoney-server skill). nginx forwards client headers (incl. `Origin`) unchanged by default.
- About 30 controller sites use `auth instanceof JwtAuthentication jwtAuth` → `jwtAuth.getUserId()`.
- Existing auth tests: `JwtServiceTest`, `JwtAuthenticationTest`, `AuthControllerTest`, `AuthServiceTest`, `AuthRulesTest`, `UnauthorizedResponseWriterTest`; `JwtService` is also used by `GlobalExceptionHandlerTest` and `ErrorResponseFormatIntegrationTest`, and every controller test that builds a `JwtAuthentication`. The builder greps `JwtService|JwtAuthentication|Bearer` under `src/test` to size the test migration. Backend suite today: 415 tests, 0 failures.
- Files touched in `security/`: `config/SecurityConfig.java` (filter wiring, `csrf` disabled at lines 51-52 stays disabled — section 4.3 replaces it), `JwtAuthenticationFilter.java`, `JwtAuthentication.java`, `services/JwtService.java`; `core/config/SwaggerConfig.java`; `core/config/CorsConfig.java`.

## 4. Design

### 4.1 Session store — Postgres table (my call)

New table `user_sessions` (migration `V7__create_user_sessions.sql`):

| column | type | note |
|---|---|---|
| `id` | bigserial PK | exposed to the user only as the row id for "log out this session" |
| `user_id` | bigint FK → users, not null | indexed |
| `token_hash` | char(64) unique, not null | SHA-256 hex of the raw token; raw token never stored |
| `created_at` | timestamptz not null | |
| `last_seen_at` | timestamptz not null | updated at most once per 5 minutes per session |
| `expires_at` | timestamptz not null | absolute limit, see 4.4 |
| `revoked_at` | timestamptz null | set on logout/revoke; row kept (soft-delete convention) |
| `user_agent` | varchar(512) | raw, truncated |
| `device_label` | varchar(100) | derived once at login, e.g. "Chrome · macOS", "Safari · iPhone" |
| `ip_address` | varchar(45) | `CF-Connecting-IP`, else `X-Real-IP`, else remote addr |
| `country` | varchar(2) null | `CF-IPCountry` |

Index: `(user_id) where revoked_at is null`.

Why not Redis: one more service on a 3.7 GB box with nothing else needing it; the lookup is one indexed
primary-key-style read per request on a table with a handful of rows per user.
Why not Spring Session JDBC: its schema stores serialized attributes, not the device columns the
session list needs, and the app is stateless REST; a small custom table fits the existing filter.

### 4.2 Token and cookie

- Login generates 32 bytes from `SecureRandom`, base64url → raw token. Stored as SHA-256 hash.
- Response sets cookie `mm_session=<raw>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=<absolute timeout>`.
  - Local dev over plain http: `Secure` controlled by a property (`app.session.cookie-secure`, default `true`, `false` in local `.env`).
- The login response body no longer contains a token; it returns the user's basic info.
- A fresh token on every login (no session fixation). Re-login never reuses a row.

### 4.3 CSRF (my call)

Cookies are sent automatically, so state-changing requests need protection:
1. `SameSite=Strict` — the browser does not attach the cookie to cross-site requests.
2. For `POST/PUT/PATCH/DELETE`, the backend rejects requests whose `Origin` header (or `Referer` when `Origin` is missing) is not in the explicit allowed-origin list. 403 with a Turkish message. Fails closed: the `*` fallback is removed (4.8), so there is always an explicit list. Requests with neither header (curl, tests) are rejected for state-changing methods except `/auth/login` and `/auth/register`, which carry no session (my call).
Spring's CSRF token is not used (would add a token round trip to every form for no extra protection here).

### 4.4 Lifetimes

- **Idle timeout:** session dies if unused for N days (`last_seen_at + idle < now`).
- **Absolute timeout:** session dies M days after login regardless of use (`expires_at`).
- Values are properties (`app.session.idle-timeout`, `app.session.absolute-timeout`). **Architect decided (2026-11-11): idle 7 days, absolute 30 days.** Cookie `Max-Age` = 30 days.

### 4.5 Request authentication

`SessionAuthenticationFilter` replaces `JwtAuthenticationFilter`, same position in the chain, same
skip list (`/auth/login`, `/auth/register`, swagger in non-prod):
1. Read `mm_session` cookie. Missing → 401 "Yetkisiz giriş. Lütfen tekrar giriş yapınız" (current wording, `UnauthorizedResponseWriter`).
2. Hash, look up an active row (`revoked_at is null and expires_at > now and last_seen_at > now - idle`). None → 401 "Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız" and clear the cookie.
3. Load the user (the session's `user_id`); a user with `is_removed = true` → treat as no session (401, revoke the row). Put a `UserSessionAuthentication` (userId + sessionId) in the `SecurityContext`.
4. If `last_seen_at` is older than 5 minutes, update it (single `UPDATE ... WHERE id = ?`).

`JwtAuthentication` is renamed to `UserSessionAuthentication` (adds `getSessionId()`); the ~30 controller
call sites change mechanically (`instanceof UserSessionAuthentication a` → `a.getUserId()`). Separate commit.

### 4.6 Endpoints

All under the existing `/auth` controller unless noted; DTO per endpoint.

| Method & path | Behaviour |
|---|---|
| `POST /auth/login` | Validates credentials (unchanged), creates session row, sets cookie, returns `{ fullName, email }` |
| `POST /auth/logout` | Revokes the current session, expires the cookie. 204/200 even if already revoked |
| `GET /auth/me` | Current user `{ id, fullName, email }`; used by the frontend on start to know if it is logged in |
| `GET /auth/sessions` | Caller's active sessions, newest `last_seen_at` first: `{ id, deviceLabel, ipAddress, country, createdAt, lastSeenAt, current }` |
| `DELETE /auth/sessions/{id}` | Revokes one of the caller's sessions. Another user's id → 404 (do not leak existence). Own current id → same as logout |
| `DELETE /auth/sessions` | Revokes all of the caller's sessions except the current one; returns the count |

Service method `revokeAllForUser(userId, exceptSessionId)` exists for a future password change.

### 4.7 Frontend

- `authSlice`: no token. State is `{ status: 'unknown' | 'authenticated' | 'anonymous', user }`. Remove the `auth_token` localStorage key (and delete any leftover value on start).
- `baseApi`: `fetchBaseQuery({ credentials: 'include' })`, no `Authorization` header. The global 401 handler (`baseApi.ts:58-86`: clear auth, toast, `navigateToLogin` after 5 s) stays for normal calls but skips two (my call):
  - `GET /auth/me` 401 → silently `anonymous`, no toast, `ProtectedRoute` redirects to `/login`.
  - `POST /auth/login` 401 (`UnauthorizedException` "Giriş başarısız…", `AuthServiceImpl.java:45`) → error shown on the login form, as today.
- App start: call `GET /auth/me`; while `unknown`, `ProtectedRoute` shows a loader instead of redirecting.
- Login page: on success store the returned user, mark authenticated. Logout button calls `POST /auth/logout` and then clears state and the RTK Query cache.
- New page **Hesabım** (Architect decided 2026-11-11), route `/account-settings` (my call; `/accounts` is taken by bank accounts), sidebar item above the language/theme buttons; future profile/password items go here. Its first section is **Oturumlar**: list from `GET /auth/sessions`, "Bu cihaz" badge on the current one, "Çıkış yap" per row (confirmation in an in-app modal, not `window.confirm`), "Diğer tüm oturumları kapat". Turkish/English i18n keys. Mobile layout 390 px.

### 4.8 Local dev and CORS (my call)

- `localhost:5174` → `127.0.0.1:8090` is cross-site, so a `SameSite=Strict` cookie would never be sent in dev.
  Dev therefore mirrors prod's same-origin setup: a Vite dev/preview proxy `/api` → `http://127.0.0.1:8090`
  (rewrite strips `/api`, since the local backend has no context path), and `VITE_API_BASE_URL=/api` in
  the frontend `.env` / `.env.example`. Prod build keeps `https://mymoney.hakandincturk.com/api`.
- `CorsConfig`: the `*` fallback is replaced by `http://localhost:5173,http://localhost:5174` (still
  `allowCredentials(true)`); prod value unchanged. With the proxy, browser requests are same-origin anyway;
  the list is what the Origin check (4.3) compares against.

### 4.9 Cut-over

**Architect decided (2026-11-11): one forced re-login.** The deploy removes JWT acceptance; every open
browser gets one 401 and lands on the login page. No transition code.

### 4.10 Removal

After cut-over: delete `JwtService`, `JwtAuthenticationFilter`, `jwt.secret.key` from `application.yaml`,
the `io.jsonwebtoken` dependencies, `JwtServiceTest`; Swagger security scheme changes from bearer to cookie
(`SwaggerConfig`). Pending-infra item "JWT secret hardcoded" closes.

## 5. Tests (backend; frontend has no test framework)

- `SessionServiceTest`: create (hash stored, raw returned once), lookup active / revoked / idle-expired / absolute-expired, `last_seen_at` throttle, revoke own / other user's (not found), revoke-all-except-current count.
- `SessionAuthenticationFilterTest`: no cookie → 401; unknown token → 401 + cookie cleared; valid → context has userId and sessionId; skip list.
- Origin check: state-changing request from a foreign origin → 403; same origin → passes; GET unaffected.
- `AuthControllerTest`: login sets an HttpOnly/SameSite cookie and returns no token; logout revokes and expires cookie; `/auth/me`; sessions list marks `current`; delete endpoints.
- Integration (MockMvc + real DB as the existing tests do): login → use cookie → revoke from a second session → first session's next request 401.
- Existing controller tests switch from bearer tokens to a session cookie helper.
- Frontend: type-check + build + headless Chrome walk-through (login, reload stays logged in, sessions screen desktop + 390 px, revoke another session, logout) per memory `ui_verification_headless.md`.

## 6. Rollout

1. Backend PR + frontend PR prepared together; both merged by the Architect.
2. Pre-deploy DB backup (as on 2026-11-11).
3. Deploy backend then frontend immediately (`deploy.sh all`). Old frontend + new backend = login broken for the seconds between; acceptable for a personal app.
4. Verify on prod: login, reload, sessions list shows device and country, revoke from a second browser.

## 7. Risks

- Cloudflare Bot Fight Mode / caching must not touch `/api/*` cookies: API responses are `DYNAMIC` today; verify `Set-Cookie` reaches the browser after deploy.
- `SameSite=Strict` cookie is not sent when the app is opened from an external link's first navigation, but the SPA's API calls are same-origin XHR, so this only matters for server-rendered pages (none).
- iOS "Add to Home Screen" keeps its own cookie jar; users log in once there.

## 7b. Calls made by Claude (check while reading)

SQL table instead of Redis/Spring Session (4.1) · SameSite=Strict + Origin check instead of CSRF tokens (4.3) · Vite proxy for dev (4.8) · `*` CORS fallback removed (4.8) · `last_seen_at` throttled to 5 min (4.5) · revoked rows kept, no purge job (4.1) · `/auth/me` and login excluded from the global 401 handler (4.7) · removed users' sessions rejected (4.5).

## 8. Left to the builder

User-agent parsing approach (a small hand-written parser for the major browsers/OSes is enough; no new
dependency unless it stays tiny), exact DTO and class names, i18n wording, how the loader looks.

## 9. Architect's decisions (2026-11-11)

1. Lifetimes: idle 7 days, absolute 30 days.
2. Cut-over: everyone logs in once more at deploy; no JWT transition.
3. Session list: a new "Hesabım" page in the sidebar.
