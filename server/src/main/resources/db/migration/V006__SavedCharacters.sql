CREATE TABLE IF NOT EXISTS "savedplayer" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "px" INT NOT NULL,
  "py" INT NOT NULL,
  "pz" INT NOT NULL,
  "orientation" INT NOT NULL,
  "zone_num" SMALLINT NOT NULL,
  "health" SMALLINT NOT NULL,
  "armor" SMALLINT NOT NULL,
  "exosuit_num" SMALLINT NOT NULL,
  "loadout" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "savedavatar" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "forget_cooldown" TIMESTAMP NOT NULL,
  "purchase_cooldowns" TEXT NOT NULL,
  "use_cooldowns" TEXT NOT NULL
);

ALTER TABLE account
ADD COLUMN last_faction_id SMALLINT DEFAULT 3
