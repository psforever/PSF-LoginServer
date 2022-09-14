CREATE TABLE IF NOT EXISTS "friend" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES account (id),
  "char_id" INT NOT NULL REFERENCES account (id)
);

CREATE TABLE IF NOT EXISTS "ignored" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES account (id),
  "char_id" INT NOT NULL REFERENCES account (id)
);
