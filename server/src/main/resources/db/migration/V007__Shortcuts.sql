CREATE TABLE IF NOT EXISTS "shortcut" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "slot" SMALLINT NOT NULL,
  "purpose" SMALLINT NOT NULL,
  "tile" VARCHAR(20) NOT NULL,
  "effect1" VARCHAR(3),
  "effect2" TEXT,
  UNIQUE(avatar_id, slot)
);
