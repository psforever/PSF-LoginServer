CREATE TABLE IF NOT EXISTS "vehicleloadout" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "loadout_number" INT NOT NULL,
  "name" VARCHAR(36) NOT NULL,
  "vehicle" SMALLINT NOT NULL,
  "items" TEXT NOT NULL
);
