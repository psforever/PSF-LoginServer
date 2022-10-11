CREATE TABLE IF NOT EXISTS "friend" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "char_id" INT NOT NULL REFERENCES avatar (id),
  UNIQUE(avatar_id, char_id)
);

CREATE TABLE IF NOT EXISTS "ignored" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "char_id" INT NOT NULL REFERENCES avatar (id),
  UNIQUE(avatar_id, char_id)
);
