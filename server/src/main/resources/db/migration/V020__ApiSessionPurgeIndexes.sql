-- The hourly purge deletes on last_seen_at_ms or created_at_ms, and api_session is otherwise indexed on
-- account_id alone, so it would scan a table that only grows between sweeps.
--
-- These indexes ship separately from V019 because a development or test database may already have applied
-- V019 while this pull request was open. Editing an applied migration changes its checksum, and Flyway
-- validates checksums, so that database would refuse to start. A separate migration serves both kinds.

CREATE INDEX IF NOT EXISTS "api_session_last_seen_at_ms_idx" ON "api_session" ("last_seen_at_ms");
CREATE INDEX IF NOT EXISTS "api_session_created_at_ms_idx" ON "api_session" ("created_at_ms");
