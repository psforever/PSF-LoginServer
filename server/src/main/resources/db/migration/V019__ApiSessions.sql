-- Sign-in sessions issued by the PSF-HTTP API, and authenticator-app (TOTP) secrets.
--
-- The web portal's own express-session table ("session") is unrelated and left alone.
--
-- Times are UTC epoch milliseconds, so the server never converts time zones and expiry is a plain
-- integer comparison.

-- One row per signed-in session. The token itself is never stored, only its SHA-256, so a copy of this
-- table cannot be used to sign in.
CREATE TABLE IF NOT EXISTS "api_session" (
  "token_digest" CHAR(64) PRIMARY KEY,
  "account_id" INT NOT NULL REFERENCES "account" ("id") ON DELETE CASCADE,
  "created_at_ms" BIGINT NOT NULL,
  "last_seen_at_ms" BIGINT NOT NULL,
  -- SHA-256 of the account's passhash when the session was issued; a password change makes it stale,
  -- which ends the session.
  "credential_fingerprint" CHAR(64) NOT NULL,
  -- Whether the account was a GM when the session was issued; losing GM ends the session.
  "gm_at_issue" BOOLEAN NOT NULL,
  -- Whether a TOTP code was checked for this session.
  "mfa" BOOLEAN NOT NULL DEFAULT FALSE
);

-- Signing out everywhere, and the per-account revocations, delete by account.
CREATE INDEX IF NOT EXISTS "api_session_account_id_idx" ON "api_session" ("account_id");

-- The hourly purge deletes on either column, so it needs both to avoid scanning the table, which only
-- ever grows between sweeps.
CREATE INDEX IF NOT EXISTS "api_session_last_seen_at_ms_idx" ON "api_session" ("last_seen_at_ms");
CREATE INDEX IF NOT EXISTS "api_session_created_at_ms_idx" ON "api_session" ("created_at_ms");

-- At most one TOTP secret per account. "enabled" is false between enrolment and the first confirmed
-- code. "last_used_step" is the 30-second step of the last code accepted; that step and every earlier
-- one are refused, so an observed code cannot be replayed.
CREATE TABLE IF NOT EXISTS "account_totp" (
  "account_id" INT PRIMARY KEY REFERENCES "account" ("id") ON DELETE CASCADE,
  "secret" VARCHAR(64) NOT NULL,
  "enabled" BOOLEAN NOT NULL DEFAULT FALSE,
  "last_used_step" BIGINT NOT NULL DEFAULT 0
);
