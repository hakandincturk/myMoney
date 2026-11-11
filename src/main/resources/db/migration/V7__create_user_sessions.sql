-- Server-side login sessions (replaces JWT).
-- One row per login; the raw token lives only in the browser's HttpOnly cookie,
-- the database keeps its SHA-256 hex digest. Rows are revoked, never deleted.
-- ON DELETE CASCADE only matters for hard deletes (tests); users are soft-deleted in production.
CREATE TABLE user_sessions (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash CHAR(64) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL,
  last_seen_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  user_agent VARCHAR(512),
  device_label VARCHAR(100),
  ip_address VARCHAR(45),
  country VARCHAR(2)
);

-- Session list per user and revoke-all only look at live rows
CREATE INDEX idx_user_sessions_user_active
    ON user_sessions (user_id)
    WHERE revoked_at IS NULL;
