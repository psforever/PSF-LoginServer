ALTER TABLE characters RENAME TO avatar;
ALTER TABLE avatar RENAME CONSTRAINT characters_account_id_fkey TO avatar_account_id_fkey;
ALTER TABLE accounts RENAME TO account;
ALTER TABLE logins RENAME TO login;
ALTER TABLE login RENAME CONSTRAINT logins_account_id_fkey TO login_account_id_fkey;
ALTER TABLE loadouts RENAME TO loadout;
ALTER TABLE loadout RENAME COLUMN characters_id TO avatar_id;
ALTER TABLE loadout RENAME CONSTRAINT loadouts_characters_id_fkey TO loadout_avatar_id_fkey;
ALTER TABLE lockers RENAME TO locker;
ALTER TABLE locker RENAME COLUMN characters_id TO avatar_id;
ALTER TABLE locker RENAME CONSTRAINT lockers_characters_id_fkey TO locker_avatar_id_fkey;
ALTER TABLE buildings RENAME TO building;

ALTER TABLE avatar
ADD COLUMN bep BIGINT NOT NULL DEFAULT 0,
ADD COLUMN cep BIGINT NOT NULL DEFAULT 0,
ADD COLUMN cosmetics INT;

CREATE TABLE certification (
  id INT NOT NULL,
  avatar_id INT NOT NULL REFERENCES avatar (id),
  PRIMARY KEY (id, avatar_id)
);

CREATE TABLE implant (
  name TEXT NOT NULL,
  avatar_id INT NOT NULL REFERENCES avatar (id),
  PRIMARY KEY (name, avatar_id)
);