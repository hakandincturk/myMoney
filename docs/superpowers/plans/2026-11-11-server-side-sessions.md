# Server-side Sessions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the 24 h JWT with revocable server-side sessions in an HttpOnly cookie, plus a "Hesabım" page listing the user's sessions with per-session logout.

**Architecture:** A `user_sessions` Postgres table holds one row per login (SHA-256 of a random token). A servlet filter resolves the `mm_session` cookie to a row and puts a `UserSessionAuthentication` (userId + sessionId) in the security context; an origin filter guards state-changing requests. The SPA stops handling tokens, uses `credentials: 'include'`, and asks `/auth/me` on start.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Security, JPA/Flyway, JUnit 5 + Mockito + Testcontainers; React + TypeScript + RTK Query + Vite.

**Spec:** `docs/superpowers/specs/2026-11-11-server-side-sessions-design.md` (read it with this plan).

**Repos / branch:** `~/code/self/java/myMoney/myMoney-backend` and `.../myMoney-frontend`, both on `feat/server-side-sessions` (from `main`, 2026-11-11).

## Global Constraints

- Cookie: name `mm_session`, `HttpOnly`, `Secure` (property `app.session.cookie-secure`, default `true`), `SameSite=Strict`, `Path=/`, `Max-Age` = 30 days.
- Lifetimes: idle 7 days (`app.session.idle-timeout=7d`), absolute 30 days (`app.session.absolute-timeout=30d`), `last_seen_at` written at most every 5 minutes (`app.session.touch-interval=5m`).
- Token: 32 bytes `SecureRandom`, base64url without padding; DB stores only lowercase hex SHA-256 (64 chars).
- Layering controller → service → repository; DTO per endpoint; error messages Turkish; code/comments English; Flyway new file only (`V7__create_user_sessions.sql`); session rows are never deleted (revoke = set `revoked_at`).
- 401 messages reuse `UnauthorizedResponseWriter`: no cookie → "Yetkisiz giriş. Lütfen tekrar giriş yapınız"; unknown/expired/revoked → "Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız".
- Origin check 403 message: "İstek kaynağı doğrulanamadı".
- No new runtime dependency (backend or frontend). `io.jsonwebtoken` dependencies are removed.
- Java builds: `export PATH="$HOME/.jenv/shims:$PATH"; export JAVA_HOME=$(jenv javahome)`. Integration tests need Docker (Testcontainers).
- Frontend has no test framework: verification = `npm run type-check`, `npm run build`, headless Chrome (memory `ui_verification_headless.md`).
- Commit per task; never push/merge/deploy without the Architect's explicit OK.

## Review Focus

1. **Revoked from another browser:** browser A's next API call gets 401 once, A lands on `/login` once, no toast/redirect loop → integration test in Task 6 + headless step in Task 9.
2. **Cookie missing or cleared while the app is open** (user cleared site data): same single redirect, no crash → Task 7 `/auth/me` handling + Task 9 headless step.
3. **Revoking a session id that belongs to another user:** 404, the other row stays active → Task 5 service test.
4. **Active user at the absolute limit:** a session used every day still dies 30 days after login → Task 2 test `authenticate_rejectsAfterAbsoluteExpiryEvenIfRecentlySeen`.
5. **Login from a browser that still has a valid cookie:** the old row is revoked, so the list does not fill with duplicates of one browser → Task 5 test `login_revokesPreviousSessionFromSameCookie`.

---

## Backend (myMoney-backend)

### Task 1: Session table, entity, repository

**Files:**
- Create: `src/main/resources/db/migration/V7__create_user_sessions.sql`
- Create: `src/main/java/com/hakandincturk/models/UserSession.java`
- Create: `src/main/java/com/hakandincturk/repositories/UserSessionRepository.java`
- Test: `src/test/java/com/hakandincturk/myMoney/integration/UserSessionRepositoryIntegrationTest.java` (extends `BaseIntegrationTest`)

**Interfaces:**
- Produces: entity `UserSession` (does NOT extend `BaseEntitiy`; fields `Long id`, `Users user` (`@ManyToOne(fetch = LAZY)`, `user_id`), `String tokenHash`, `Instant createdAt`, `Instant lastSeenAt`, `Instant expiresAt`, `Instant revokedAt`, `String userAgent`, `String deviceLabel`, `String ipAddress`, `String country`).
- Produces: `UserSessionRepository extends JpaRepository<UserSession, Long>` with
  - `Optional<UserSession> findByTokenHashAndRevokedAtIsNull(String tokenHash)`
  - `List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterAndLastSeenAtAfterOrderByLastSeenAtDesc(Long userId, Instant now, Instant idleCutoff)`
  - `Optional<UserSession> findByIdAndUserIdAndRevokedAtIsNull(Long id, Long userId)`
  - `@Modifying @Query int revokeAllForUserExcept(Long userId, Long exceptId, Instant now)` (`exceptId` may be null → revoke all)
  - `@Modifying @Query int touch(Long id, Instant now)` (`update ... set lastSeenAt = :now where id = :id`)

- [ ] **Step 1:** Write `V7__create_user_sessions.sql`: columns exactly as spec 4.1 (`timestamptz` for the four times, `char(64) unique not null` hash, `varchar(512)` UA, `varchar(100)` label, `varchar(45)` IP, `varchar(2)` country, FK `user_id → users(id) not null`) + `create index idx_user_sessions_user_active on user_sessions (user_id) where revoked_at is null;`
- [ ] **Step 2:** Write failing integration tests: `findByTokenHash_ignoresRevoked`, `listActive_excludesExpiredIdleAndRevoked_newestFirst`, `findByIdAndUserId_doesNotReturnOtherUsersRow`, `revokeAllForUserExcept_keepsCurrentAndOtherUsers` (asserts returned count and rows' `revokedAt`).
- [ ] **Step 3:** Run `./mvnw -q test -Dtest=UserSessionRepositoryIntegrationTest` → FAIL (classes missing).
- [ ] **Step 4:** Implement entity and repository (`ddl-auto: validate` must accept the mapping: `Instant` ↔ `timestamptz`, `@Column(columnDefinition = "char(64)")` for the hash).
- [ ] **Step 5:** Re-run → PASS. Commit `feat(session): add user_sessions table, entity and repository`.

### Task 2: Session service, properties, device label

**Files:**
- Create: `src/main/java/com/hakandincturk/security/session/SessionProperties.java` (`@ConfigurationProperties("app.session")`, record or class: `Duration idleTimeout = 7d`, `Duration absoluteTimeout = 30d`, `Duration touchInterval = 5m`, `boolean cookieSecure = true`, `String cookieName = "mm_session"`), enable via `@EnableConfigurationProperties` on the security config.
- Create: `src/main/java/com/hakandincturk/core/config/ClockConfig.java` (`@Bean Clock clock()` → `Clock.systemUTC()`; none exists today).
- Create: `src/main/java/com/hakandincturk/security/session/SessionTokens.java` (static `String newRawToken()`, `String hash(String raw)`).
- Create: `src/main/java/com/hakandincturk/security/session/DeviceLabels.java` (static `String fromUserAgent(String ua)` → e.g. "Chrome · macOS", "Safari · iPhone", "Firefox · Windows", "Edge · Windows", "Bilinmeyen cihaz" for null/unknown; check Edge before Chrome, Chrome before Safari).
- Create: `src/main/java/com/hakandincturk/services/abstracts/SessionService.java`, `src/main/java/com/hakandincturk/services/impl/SessionServiceImpl.java`
- Create: `src/main/java/com/hakandincturk/security/session/ClientInfo.java` (record `String userAgent, String ipAddress, String country`).
- Test: `src/test/java/com/hakandincturk/myMoney/service/SessionServiceTest.java`, `src/test/java/com/hakandincturk/myMoney/security/DeviceLabelsTest.java`, `src/test/java/com/hakandincturk/myMoney/security/SessionTokensTest.java`

**Interfaces:**
- Consumes: Task 1 repository.
- Produces (`SessionService`):
  - `CreatedSession create(Users user, ClientInfo client)` — `record CreatedSession(String rawToken, UserSession session)`; `expiresAt = now + absoluteTimeout`, `lastSeenAt = createdAt = now`.
  - `Optional<UserSession> authenticate(String rawToken)` — active = not revoked, `expiresAt > now`, `lastSeenAt > now - idleTimeout`, `user.isRemoved == false` (a removed user's row is revoked here); touches when `lastSeenAt < now - touchInterval`.
  - `List<UserSession> listActive(Long userId)`
  - `void revoke(Long userId, Long sessionId)` — throws `NotFoundException("Oturum bulunamadı")` when not the caller's active row.
  - `void revokeCurrent(Long sessionId)` — idempotent, no exception if already revoked.
  - `int revokeAllExcept(Long userId, Long currentSessionId)`

- [ ] **Step 1:** Failing tests (`Clock.fixed`, mocked repository):
  - `SessionTokensTest`: raw token decodes to 32 bytes; `hash("abc")` = `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`.
  - `DeviceLabelsTest`: one UA string per label above, plus null → "Bilinmeyen cihaz".
  - `SessionServiceTest`: `create_storesHashNotRawToken_andSetsExpiry30Days`, `authenticate_rejectsRevoked`, `authenticate_rejectsIdleOver7Days`, `authenticate_rejectsAfterAbsoluteExpiryEvenIfRecentlySeen`, `authenticate_rejectsAndRevokesRemovedUser`, `authenticate_touchesOnlyWhenOlderThan5Minutes` (4 min → no `touch`, 6 min → `touch`), `revoke_otherUsersSession_throwsNotFound`, `revokeCurrent_alreadyRevoked_noException`, `revokeAllExcept_passesCurrentId`.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement. **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(session): add session service, token hashing and device labels`.

### Task 3: Rename `JwtAuthentication` → `UserSessionAuthentication`

**Files:**
- Rename: `src/main/java/com/hakandincturk/security/JwtAuthentication.java` → `UserSessionAuthentication.java` (adds `Long sessionId` + `getSessionId()`; constructor `(Object principal, Collection<? extends GrantedAuthority> authorities, Long userId, Long sessionId)`).
- Modify: the 7 controllers with `instanceof JwtAuthentication` (30 sites), `JwtAuthenticationFilter` (temporary, passes `null` sessionId), all tests listed by `grep -rln JwtAuthentication src/test`; rename `JwtAuthenticationTest` → `UserSessionAuthenticationTest`.

**Interfaces:**
- Produces: `UserSessionAuthentication#getUserId()`, `#getSessionId()`.

- [ ] **Step 1:** Mechanical rename (variable `jwtAuth` → `userAuth`). No behaviour change.
- [ ] **Step 2:** `./mvnw -q test` → same count as before (415) + Task 1/2 tests, 0 failures. `grep -rn JwtAuthentication src` → only `JwtAuthenticationFilter` class name left.
- [ ] **Step 3:** Commit `refactor(auth): rename JwtAuthentication to UserSessionAuthentication`.

### Task 4: Session filter, origin filter, security wiring

**Files:**
- Create: `src/main/java/com/hakandincturk/security/SessionAuthenticationFilter.java` (`OncePerRequestFilter`; same `shouldNotFilter` paths as the JWT filter: login, register, swagger paths).
- Create: `src/main/java/com/hakandincturk/security/OriginCheckFilter.java` (`OncePerRequestFilter`; only `POST/PUT/PATCH/DELETE`; skips `/auth/login`, `/auth/register`; compares `Origin`, else scheme+host of `Referer`, against the CORS origin list; missing both → 403).
- Create: `src/main/java/com/hakandincturk/security/session/SessionCookies.java` (`@Component`: `ResponseCookie issue(String raw)`, `ResponseCookie clear()`, `Optional<String> read(HttpServletRequest)`; uses `SessionProperties`).
- Create: `src/main/java/com/hakandincturk/security/session/ClientInfoResolver.java` (`ClientInfo resolve(HttpServletRequest)`: IP from `CF-Connecting-IP` → `X-Real-IP` → `getRemoteAddr()`; country `CF-IPCountry` (2 chars, else null); UA truncated to 512).
- Modify: `src/main/java/com/hakandincturk/security/config/SecurityConfig.java` (add `OriginCheckFilter` then `SessionAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`; remove JWT filter; keep `csrf.disable()` and `STATELESS`).
- Modify: `src/main/java/com/hakandincturk/core/config/CorsConfig.java` — expose `List<String> allowedOrigins()` for the origin filter; default `${cors.allowed-origins:http://localhost:5173,http://localhost:5174}` (no `*`).
- Delete: `src/main/java/com/hakandincturk/security/JwtAuthenticationFilter.java`.
- Test: `src/test/java/com/hakandincturk/myMoney/security/SessionAuthenticationFilterTest.java`, `.../security/OriginCheckFilterTest.java`, update `src/test/java/com/hakandincturk/myMoney/integration/ErrorResponseFormatIntegrationTest.java`, update `src/test/java/com/hakandincturk/myMoney/config/CorsConfigTest.java`.

**Interfaces:**
- Consumes: `SessionService.authenticate`, `UserSessionAuthentication`, `UnauthorizedResponseWriter.write`.
- Produces: `SessionCookies.issue/clear/read`, `ClientInfoResolver.resolve` (used by Task 5).

- [ ] **Step 1:** Failing tests:
  - Filter: `noCookie_returns401WithYetkisizMessage`, `unknownToken_returns401AndClearsCookie` (response has `Set-Cookie: mm_session=; Max-Age=0`), `validToken_setsUserSessionAuthenticationWithUserAndSessionId`, `loginPath_isNotFiltered`.
  - Origin: `post_fromForeignOrigin_returns403`, `post_fromAllowedOrigin_passes`, `post_withRefererOnly_allowed_passes`, `post_withoutOriginOrReferer_returns403`, `get_fromForeignOrigin_passes`, `loginPost_withoutOrigin_passes`.
  - `ErrorResponseFormatIntegrationTest`: replace the bearer cases with "invalid `mm_session` cookie → 401 + ISO timestamp" and "no cookie → 401 + ISO timestamp".
  - `CorsConfigTest`: default list is the two localhost origins; `*` no longer the default.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement + wire. **Step 4:** `./mvnw -q test` → PASS (controller unit tests unaffected; they set the context directly).
- [ ] **Step 5:** Commit `feat(auth): authenticate requests from the session cookie and check request origin`.

### Task 5: Auth endpoints (login, logout, me, sessions)

**Files:**
- Modify: `src/main/java/com/hakandincturk/services/abstracts/AuthService.java`, `services/impl/AuthServiceImpl.java`, `webapi/controllers/abstracts/AuthController.java`, `webapi/controllers/impl/AuthControllerImpl.java`
- Modify: `src/main/java/com/hakandincturk/dtos/auth/response/LoginResponseDto.java` → fields `String fullName, String email` (no token).
- Create: `src/main/java/com/hakandincturk/dtos/auth/response/MeResponseDto.java` (`Long id, String fullName, String email`), `SessionResponseDto.java` (`Long id, String deviceLabel, String ipAddress, String country, Instant createdAt, Instant lastSeenAt, boolean current`), `RevokeSessionsResponseDto.java` (`int revokedCount`).
- Test: `src/test/java/com/hakandincturk/myMoney/controller/AuthControllerTest.java`, `src/test/java/com/hakandincturk/myMoney/service/AuthServiceTest.java`

**Interfaces:**
- Consumes: `SessionService` (Task 2), `SessionCookies`, `ClientInfoResolver` (Task 4), `UserSessionAuthentication` (Task 3).
- Produces (HTTP, used by the frontend):
  - `POST /auth/login` body `{email,password}` → `Set-Cookie mm_session`, `ApiResponse<LoginResponseDto>`
  - `POST /auth/logout` → expires cookie, `ApiResponse<Void>` "Çıkış yapıldı"
  - `GET /auth/me` → `ApiResponse<MeResponseDto>`
  - `GET /auth/sessions` → `ApiResponse<List<SessionResponseDto>>`
  - `DELETE /auth/sessions/{id}` → `ApiResponse<Void>` "Oturum kapatıldı" (own current id → also expires cookie)
  - `DELETE /auth/sessions` → `ApiResponse<RevokeSessionsResponseDto>` "Diğer oturumlar kapatıldı"
- `/auth/logout` is NOT in the filter's skip list (needs the session); `SecurityConfig` permit list stays login + register.

- [ ] **Step 1:** Failing tests:
  - `login_setsHttpOnlyStrictCookie_andReturnsNoToken` (cookie string contains `HttpOnly`, `SameSite=Strict`, `Max-Age=2592000`).
  - `login_revokesPreviousSessionFromSameCookie` (request carries a valid `mm_session` → `revokeCurrent(oldId)` called).
  - `login_wrongPassword_throwsUnauthorized_noCookie`.
  - `logout_revokesCurrent_andClearsCookie`; `logout_whenAlreadyRevoked_returnsSuccess`.
  - `me_returnsCurrentUser`.
  - `sessions_marksOnlyCurrentAsCurrent_newestFirst`.
  - `revoke_otherUsersSessionId_returns404_andRowStaysActive` (service level with mocked repo returning empty).
  - `revokeOthers_returnsCount_keepsCurrent`.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement (controller takes `HttpServletRequest`/`HttpServletResponse`, adds the cookie header via `response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())`). **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(auth): session-based login, logout, me and session management endpoints`.

### Task 6: Remove JWT, Swagger scheme, end-to-end test

**Files:**
- Delete: `src/main/java/com/hakandincturk/security/services/JwtService.java`, `src/test/java/com/hakandincturk/myMoney/security/JwtServiceTest.java`
- Modify: `pom.xml` (remove the 3 `jjwt-*` dependencies), `src/main/resources/application.yaml` (remove the `jwt:` block; add `app.session.*` defaults from Global Constraints), `.env.example` (add `APP_SESSION_COOKIE_SECURE=false` commented for local http), `src/main/java/com/hakandincturk/core/config/SwaggerConfig.java` (scheme `sessionCookie`: `Type.APIKEY`, `In.COOKIE`, name `mm_session`), `GlobalExceptionHandlerTest`, `AuthServiceTest` (drop `JwtService` mocks).
- Test: `src/test/java/com/hakandincturk/myMoney/integration/SessionFlowIntegrationTest.java` (`@AutoConfigureMockMvc`, extends `BaseIntegrationTest`)

- [ ] **Step 1:** Failing integration test `revokedFromSecondSession_firstSessionGets401`: register → login twice (cookies A, B, `Origin: http://localhost:5174`) → `GET /auth/sessions` with A lists 2, A is `current` → `DELETE /auth/sessions/{A.id}` with B → `GET /auth/me` with A → 401 "Oturumunuzun süresi dolmuştur…"; with B → 200. Plus `logout_thenSameCookie_gets401`.
- [ ] **Step 2:** Run → FAIL (or PASS if Tasks 4-5 already cover it; then the test still stays as the regression pin). **Step 3:** Remove JWT code/deps/config.
- [ ] **Step 4:** `grep -rni "jwt\|jsonwebtoken\|Bearer" src pom.xml` → no hits outside comments/changelog. `./mvnw -q clean verify` → Tests run: N, Failures: 0, Errors: 0.
- [ ] **Step 5:** Commit `chore(auth): remove JWT code, dependencies and config`.

## Frontend (myMoney-frontend)

### Task 7: Cookie auth plumbing

**Files:**
- Modify: `vite.config.ts` — `server.proxy` and `preview.proxy`: `'/api' → { target: env.VITE_DEV_API_TARGET || 'http://127.0.0.1:8090', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') }`.
- Modify: `.env.example` (`VITE_API_BASE_URL=/api`, `VITE_DEV_API_TARGET=http://127.0.0.1:8090`); local `.env` same (git-ignored).
- Modify: `src/config/environment.ts`, `src/services/baseApi.ts` (default base URL `/api`; `credentials: 'include'`; drop `prepareHeaders` token; global 401 handler skips requests whose url is `ApiUrl.AUTH_ME` or `ApiUrl.AUTH_LOGIN`).
- Modify: `src/config/ApiUrl.ts` (add `AUTH_ME = '/auth/me'`, `AUTH_SESSIONS = '/auth/sessions'`; `AUTH_LOGOUT` exists).
- Modify: `src/store/slices/authSlice.ts` — state `{ status: 'unknown' | 'authenticated' | 'anonymous'; user: { id?: number; fullName: string; email: string } | null }`; actions `setUser(user)`, `setAnonymous()`, `clearAuth()` (= anonymous); on module load `localStorage.removeItem('auth_token')`. `src/store/slices/authSelectors.ts` (`selectAuthStatus`, `selectUser`; remove `selectToken`).
- Modify: `src/services/authApi.ts` — `login` returns `{fullName,email}`; add `me` query, `logout` mutation, `sessions` query (tag `Sessions`), `revokeSession(id)` and `revokeOtherSessions()` mutations invalidating `Sessions`.
- Create: `src/components/AuthBootstrap.tsx` — on mount calls `me`; success → `setUser`, error → `setAnonymous`. Wraps routes in `App`/`main`.
- Modify: `src/components/ProtectedRoute.tsx` (status `unknown` → full-page `Skeleton`/spinner; `anonymous` + requireAuth → `/login`; `authenticated` + !requireAuth → `/`).
- Modify: `src/pages/screens/auth/LoginPage.tsx:58` (`setUser(result.data)`), `src/pages/layouts/AppLayout.tsx:88` (logout: `await logout()` mutation, then `clearAuth()` and `dispatch(api.util.resetApiState())` for every API slice).

- [ ] **Step 1:** Implement. **Step 2:** `npm run type-check` → 0 errors; `grep -rn "auth_token\|Bearer\|auth.token" src` → only the one-time `removeItem`.
- [ ] **Step 3:** Commit `feat(auth): cookie-based session auth in the SPA`.

### Task 8: "Hesabım" page with sessions

**Files:**
- Create: `src/pages/screens/AccountSettingsPage.tsx`, `src/components/account-settings/SessionsPanel.tsx`
- Modify: `src/pages/routes/AppRoutes.tsx` (route `/account-settings`, protected, inside `AppLayout` like `/reports`), `src/pages/layouts/AppLayout.tsx` (sidebar item after Raporlar, icon `faUserGear`, label `t('sidebar.accountSettings')`), `src/i18n/locales/tr.json`, `src/i18n/locales/en.json`, `src/types` (`AuthDTOs.Session` matching `SessionResponseDto`).

Copy (tr / en): sidebar "Hesabım" / "My account"; section "Oturumlar" / "Sessions"; badge "Bu cihaz" / "This device"; row button "Çıkış yap" / "Log out"; bulk button "Diğer tüm oturumları kapat" / "Log out all other sessions"; confirm title "Oturum kapatılsın mı?" / "Log out this session?"; empty "Başka aktif oturum yok" / "No other active sessions"; row meta "Son etkinlik {{time}} · {{ip}} · {{country}}".

Behaviour: list newest first (server order); current row first-class badge, its button logs out locally (same as sidebar logout); confirm via existing `src/components/ui/Modal.tsx`; success toast via the existing `showToast` event; bulk button hidden when only the current session exists; relative time via the project's existing date utils (`src/utils/formatters`), else `Intl.RelativeTimeFormat`. Mobile 390 px: rows stack, buttons full width. Follow `ui_style_preferences` (no borders/glow; solid pastel, separators).

- [ ] **Step 1:** Implement. **Step 2:** `npm run type-check` + `npm run build` → clean.
- [ ] **Step 3:** Commit `feat(account): Hesabım page with active sessions and remote logout`.

## Verification

### Task 9: Full verification (no code unless a check fails)

- [ ] **Step 1:** Backend: `./mvnw -q clean verify` → record `Tests run: N, Failures: 0, Errors: 0`.
- [ ] **Step 2:** Local stack: backend on 8090 with `APP_SESSION_COOKIE_SECURE=false`; `npx vite build && npx vite preview --port 5174 --strictPort` (proxy active).
- [ ] **Step 3:** Headless Chrome (puppeteer-core, scratchpad), two browser contexts A and B with a throwaway local user:
  1. A logs in → reload → still on `/` (session survives reload); `document.cookie` does not contain `mm_session` (HttpOnly).
  2. B logs in; A opens `/account-settings` → 2 rows, A marked "Bu cihaz"; screenshots desktop 1440 and mobile 390×844.
  3. A revokes B's row → B's next navigation lands on `/login` exactly once (count `navigateToLogin`/URL changes), no repeated toasts.
  4. A clears cookies via CDP → next navigation → `/login`, no crash.
  5. A logs out → `/login`; a request with the old cookie value gets 401.
  6. `/reports` and `/` still render data (regression).
- [ ] **Step 4:** Remove the throwaway local user(s); stop local servers.
- [ ] **Step 5:** Report to the Architect with screenshots path; ask for push/PR/merge/deploy OK (rollout = spec section 6: backup → `deploy.sh all` → prod check incl. `Set-Cookie` through Cloudflare).
