CREATE DATABASE psforever
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'English_United States.1252'
       LC_CTYPE = 'English_United States.1252'
       CONNECTION LIMIT = -1;

CREATE TABLE IF NOT EXISTS "accounts" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "username" VARCHAR(64) NOT NULL UNIQUE,
  "passhash" VARCHAR(64) NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "last_modified" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "inactive" BOOLEAN NOT NULL DEFAULT FALSE,
  "gm" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "characters" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "name" VARCHAR(64) NOT NULL,
  "account_id" INT NOT NULL REFERENCES accounts (id),
  "faction_id" INT NOT NULL,
  "gender_id" INT NOT NULL,
  "head_id" INT NOT NULL,
  "voice_id" INT NOT NULL,
  "created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "last_login" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "last_modified" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "deleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "logins" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "account_id" INT NOT NULL REFERENCES accounts (id),
  "login_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "ip_address" VARCHAR(32) NOT NULL,
  "canonical_hostname" VARCHAR(132) NOT NULL,
  "hostname" VARCHAR(132) NOT NULL,
  "port" INT NOT NULL
);

CREATE TABLE IF NOT EXISTS "loadouts" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "characters_id" INT NOT NULL REFERENCES characters (id),
  "loadout_number" INT NOT NULL,
  "exosuit_id" INT NOT NULL,
  "name" VARCHAR(36) NOT NULL,
  "items" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "lockers" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "characters_id" INT NOT NULL REFERENCES characters (id),
  "items" TEXT NOT NULL
);

--These triggers update the last_modified timestamp column when a table is updated
CREATE OR REPLACE FUNCTION fn_set_last_modified_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_modified = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_accounts_set_last_modified on accounts;
CREATE TRIGGER trigger_accounts_set_last_modified
BEFORE UPDATE ON accounts
FOR EACH ROW
EXECUTE PROCEDURE fn_set_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_players_set_last_modified on characters;
CREATE TRIGGER trigger_players_set_last_modified
BEFORE UPDATE ON characters
FOR EACH ROW
EXECUTE PROCEDURE fn_set_last_modified_timestamp();
