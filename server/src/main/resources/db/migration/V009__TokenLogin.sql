ALTER TABLE "account"
    ADD COLUMN IF NOT EXISTS "password" VARCHAR(60) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS "token" VARCHAR(31) NULL UNIQUE,
    ADD COLUMN IF NOT EXISTS "token_created" TIMESTAMP NULL;

CREATE OR REPLACE FUNCTION fn_set_token_created_timestamp()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
BEGIN
    NEW."token_created" = NOW();
    RETURN NEW;
END;
$function$
;

CREATE OR REPLACE TRIGGER trigger_accounts_set_token_created
BEFORE UPDATE
    OF "token" ON "account"
    FOR EACH ROW EXECUTE PROCEDURE fn_set_token_created_timestamp();

CREATE TABLE IF NOT EXISTS "launcher" (
    "id" SERIAL PRIMARY KEY,
    "version" TEXT NOT NULL UNIQUE,
    "released_at" TIMESTAMPTZ NOT NULL,
    "hash" TEXT NOT NULL,
    "active" BOOL NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "filehash" (
    "mode" INT NOT NULL DEFAULT 0,
    "file" TEXT NOT NULL,
    "hash" TEXT NOT NULL,
    CONSTRAINT "filehash_mode_file_key" UNIQUE ("mode", "file")
);
