-- Indexes for foreign keys that are queried on the login and gameplay hot paths.
--
-- PostgreSQL does not create an index for a REFERENCES constraint, so each foreign key that
-- is filtered on needs one declared explicitly or the lookup is a sequential scan.
--
-- Deliberately omitted, because an existing constraint already provides a usable btree with
-- avatar_id as its leading column:
--   friend, ignored              UNIQUE(avatar_id, char_id)
--   shortcut                     UNIQUE(avatar_id, slot)
--   avatarmodepermission         UNIQUE(avatar_id)
--   savedplayer, savedavatar     avatar_id is the primary key
-- Adding further indexes there would only cost write throughput.

-- Read for every character on the character select screen, and rewritten whenever a loadout
-- is saved or deleted. The primary key is id alone, so it does not serve avatar_id lookups.
CREATE INDEX IF NOT EXISTS "loadout_avatar_id_idx" ON "loadout" ("avatar_id");
CREATE INDEX IF NOT EXISTS "vehicleloadout_avatar_id_idx" ON "vehicleloadout" ("avatar_id");

-- Read on login and rewritten on every locker change.
CREATE INDEX IF NOT EXISTS "locker_avatar_id_idx" ON "locker" ("avatar_id");

-- Primary key is (id, avatar_id), so a lookup by avatar_id alone cannot use it.
CREATE INDEX IF NOT EXISTS "certification_avatar_id_idx" ON "certification" ("avatar_id");

-- Primary key is (name, avatar_id); loading every implant for an avatar does not supply
-- name and so cannot use the leading column.
CREATE INDEX IF NOT EXISTS "implant_avatar_id_idx" ON "implant" ("avatar_id");

-- Scanned to build the character list for an account.
CREATE INDEX IF NOT EXISTS "avatar_account_id_idx" ON "avatar" ("account_id");

-- killactivity is an append-only kill log that grows for the lifetime of the server, and the
-- campaign KDA query reads it on every single login with
--   WHERE (killer_id = ? OR victim_id = ?) AND killer_id <> victim_id
-- Two single-column indexes let the OR resolve as a bitmap union, keeping that query
-- proportional to one player's kills rather than to every kill the server has recorded.
CREATE INDEX IF NOT EXISTS "killactivity_killer_id_idx" ON "killactivity" ("killer_id");
CREATE INDEX IF NOT EXISTS "killactivity_victim_id_idx" ON "killactivity" ("victim_id");
