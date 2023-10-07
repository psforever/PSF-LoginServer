INSERT INTO account (id, username, passhash)
VALUES (0, 'PSForever', '');

INSERT INTO avatar (id, name, account_id, faction_id, gender_id, head_id, voice_id)
VALUES (0, 'PSForever', 0, 0, 0, 0, 0);

ALTER TABLE account
ADD COLUMN avatar_logged_in Int NOT NULL REFERENCES avatar (id) DEFAULT 0,
ADD COLUMN session_id Int NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS "sessionnumber" (
  "avatar_id" Int NOT NULL REFERENCES avatar (id),
  "session_id" Int NOT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(avatar_id, session_id)
);

CREATE TABLE IF NOT EXISTS "killactivity" (
  "index" SERIAL PRIMARY KEY NOT NULL,
  "killer_id" INT NOT NULL REFERENCES avatar (id),
  "victim_id" INT NOT NULL REFERENCES avatar (id),
  "victim_exosuit" SMALLINT NOT NULL,
  "victim_mounted" INT NOT NULL DEFAULT 0,
  "weapon_id" SMALLINT NOT NULL,
  "zone_id" SMALLINT NOT NULL,
  "px" INT NOT NULL,
  "py" INT NOT NULL,
  "pz" INT NOT NULL,
  "exp" INT NOT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "kda" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "kills" INT NOT NULL DEFAULT 0,
  "deaths" INT NOT NULL DEFAULT 0,
  "revives" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id)
);

CREATE TABLE IF NOT EXISTS "kdasession" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "session_id" INT NOT NULL,
  "kills" INT NOT NULL DEFAULT 0,
  "deaths" INT NOT NULL DEFAULT 0,
  "revives" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id, session_id)
);

CREATE TABLE IF NOT EXISTS "weaponstat" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "weapon_id" SMALLINT NOT NULL,
  "shots_fired" INT NOT NULL DEFAULT 0,
  "shots_landed" INT NOT NULL DEFAULT 0,
  "kills" INT NOT NULL DEFAULT 0,
  "assists" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id, weapon_id)
);

CREATE TABLE IF NOT EXISTS "weaponstatsession" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "session_id" INT NOT NULL,
  "weapon_id" SMALLINT NOT NULL,
  "shots_fired" INT NOT NULL DEFAULT 0,
  "shots_landed" INT NOT NULL DEFAULT 0,
  "kills" INT NOT NULL DEFAULT 0,
  "assists" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id, session_id, weapon_id)
);

CREATE TABLE IF NOT EXISTS "legacykills" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "kills" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id)
);

CREATE TABLE IF NOT EXISTS "machinedestroyed" (
  "index" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "weapon_id" INT NOT NULL,
  "machine_type" INT NOT NULL,
  "machine_faction" SMALLINT NOT NULL,
  "hacked_faction" SMALLINT NOT NULL,
  "as_cargo" BOOLEAN NOT NULL,
  "zone_num" SMALLINT NOT NULL,
  "px" INT NOT NULL,
  "py" INT NOT NULL,
  "pz" INT NOT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "assistactivity" (
  "index" SERIAL PRIMARY KEY NOT NULL,
  "victim_id" INT NOT NULL REFERENCES avatar (id),
  "attacker_id" INT NOT NULL REFERENCES avatar (id),
  "weapon_id" SMALLINT NOT NULL,
  "zone_id" SMALLINT NOT NULL,
  "px" INT NOT NULL,
  "py" INT NOT NULL,
  "pz" INT NOT NULL,
  "exp" INT NOT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "supportactivity" (
  "index" SERIAL PRIMARY KEY NOT NULL,
  "user_id" INT NOT NULL REFERENCES avatar (id), -- player that provides the support
  "target_id" INT NOT NULL REFERENCES avatar (id), -- benefactor of the support
  "target_exosuit" SMALLINT NOT NULL, -- benefactor's exo-suit
  "interaction_type" SMALLINT NOT NULL, -- classification of support
  "intermediate_type" INT DEFAULT 0, -- through what medium user_id supports target_id
  "implement_type" INT DEFAULT 0, -- tool utilized by user_id to support target_id, potentially via interaction with intermediate_type
  "exp" INT NOT NULL,
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "ntuactivity" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "zone_id" INT NOT NULL,
  "building_id" INT NOT NULL,
  "exp" INT NOT NULL,
  UNIQUE(avatar_id, zone_id, building_id)
);

CREATE TABLE IF NOT EXISTS "buildingcapture" (
  "index" SERIAL PRIMARY KEY NOT NULL,
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "zone_id" INT NOT NULL,
  "building_id" INT NOT NULL,
  "exp" INT NOT NULL,
  "exp_type" CHAR(3) NOT NULL, /* bep, cep, llu */
  "timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "expbuildingcapture" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "zone_id" INT NOT NULL,
  "building_id" INT NOT NULL,
  "captures" INT NOT NULL,
  "bep" INT NOT NULL,
  "cep" INT NOT NULL,
  UNIQUE(avatar_id, zone_id, building_id)
);

CREATE TABLE IF NOT EXISTS "llubuildingcapture" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "zone_id" INT NOT NULL,
  "building_id" INT NOT NULL,
  "captures" INT NOT NULL,
  "exp" INT NOT NULL,
  UNIQUE(avatar_id, zone_id, building_id)
);

CREATE TABLE IF NOT EXISTS "progressiondebt" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "experience" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id)
);

/*
Procedure for initializing and increasing the session number.
Index for a new session only if last session was created more than one hour ago.
*/
CREATE OR REPLACE FUNCTION proc_sessionnumber_test
(avatarId integer)
RETURNS integer
AS
$$
DECLARE time TIMESTAMP;
DECLARE number integer;
BEGIN
  SELECT MAX(session_id) INTO number
  FROM sessionnumber
  WHERE avatar_id = avatarId;
  SELECT COALESCE(timestamp) INTO time
  FROM sessionnumber
  WHERE avatar_id = avatarId AND session_id = number;
  IF (time IS null) THEN
    number := 0;
  END IF;
  RETURN number;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_sessionnumber_initAndOrIncreasePerHour
(avatarId integer)
RETURNS integer
AS
$$
DECLARE time TIMESTAMP;
DECLARE number integer;
DECLARE nextNumber integer;
BEGIN
  SELECT MAX(session_id) INTO number
  FROM sessionnumber
  WHERE avatar_id = avatarId;
  SELECT COALESCE(timestamp) INTO time
  FROM sessionnumber
  WHERE avatar_id = avatarId AND session_id = number;
  IF (time IS null) THEN
    nextNumber := 1;
    INSERT INTO sessionnumber
    VALUES (avatarId, nextNumber);
  ELSIF (CURRENT_TIMESTAMP > DATE_TRUNC('hour', time) + interval '1' hour) THEN
    nextNumber := number + 1;
    INSERT INTO sessionnumber
    VALUES (avatarId, nextNumber);
  ELSE
    nextNumber := number;
    UPDATE sessionnumber
    SET timestamp = CURRENT_TIMESTAMP
    WHERE avatar_id = avatarId AND session_id = number;
  END IF;
  RETURN nextNumber;
END;
$$ LANGUAGE plpgsql;

/*
Procedure for accessing any existing session number.
Actually polls the previous session number from the account table.
*/
CREATE OR REPLACE FUNCTION proc_sessionnumber_get
(avatarId integer)
RETURNS integer
AS
$$
DECLARE sessionId integer;
DECLARE number integer;
BEGIN
  SELECT COALESCE(session_id) INTO sessionId
  FROM account
  WHERE avatar_logged_in = avatarId;
  IF sessionId IS NULL THEN
    number := 0;
  ELSE
    number := sessionId;
  END IF;
  RETURN number;
END;
$$ LANGUAGE plpgsql;

/*
Procedures for ensuring that row entries can be found in specified tables before some other DML operation updates those row entries.
*/
CREATE OR REPLACE FUNCTION proc_kda_addEntryIfNone
(avatarId integer)
RETURNS integer
AS
$$
DECLARE out integer;
BEGIN
  IF EXISTS(
    SELECT *
    FROM kda
    WHERE avatar_id = avatarId
    HAVING COUNT(*) = 0) THEN
      INSERT INTO kda (avatar_id)
      VALUES (avatarId);
    out := 1;
  ELSE
    out := 0;
  END IF;
  RETURN out;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_weaponstat_addEntryIfNone
(avatarId integer, weaponId integer)
RETURNS integer
AS
$$
DECLARE out integer;
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, weapon_id
    FROM weaponstat
    WHERE avatar_id = avatarId AND weapon_id = weaponId
    GROUP BY avatar_id, weapon_id
  ) THEN
    INSERT INTO weaponstat (avatar_id, weapon_id)
    VALUES (avatarId, weaponId);
    out := 1;
  ELSE
    out := 0;
  END IF;
  RETURN out;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_weaponstatsession_addEntryIfNoneWithSessionId
(avatarId integer, weaponId integer, sessionId integer)
RETURNS integer
AS
$$
DECLARE out integer;
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, session_id, weapon_id
    FROM weaponstatsession
    WHERE avatar_id = avatarId AND weapon_id = weaponId AND session_id = sessionId
    GROUP BY avatar_id, session_id, weapon_id
  ) THEN
    INSERT INTO weaponstatsession (avatar_id, session_id, weapon_id)
    VALUES (avatarId, sessionId, weaponId);
    out := 1;
  ELSE
    out := 0;
  END IF;
  RETURN out;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_weaponstatsession_addEntryIfNone
(avatarId integer, weaponId integer)
RETURNS integer
AS
$$
DECLARE sessionId Int;
BEGIN
  sessionId := proc_sessionnumber_get(avatarId);
  RETURN proc_weaponstatsession_addEntryIfNoneWithSessionId(avatarId, weaponId, sessionId);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_expbuildingcapture_addEntryIfNone
(avatarId integer, buildingId integer, zoneId integer)
RETURNS integer
AS
$$
DECLARE out integer;
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, building_id, zone_id
    FROM expbuildingcapture
    WHERE avatar_id = avatarId AND building_id = buildingId AND zone_id = zoneId
    GROUP BY avatar_id, building_id, zone_id
  ) THEN
    INSERT INTO expbuildingcapture (avatar_id, building_id, zone_id, captures, bep, cep)
    VALUES (avatarId, building_id, zone_id, 0, 0, 0);
    out := 1;
  ELSE
    out := 0;
  END IF;
  RETURN out;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION proc_llubuildingcapture_addEntryIfNone
(avatarId integer, buildingId integer, zoneId integer)
RETURNS integer
AS
$$
DECLARE out integer;
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, building_id, zone_id
    FROM llubuildingcapture
    WHERE avatar_id = avatarId AND building_id = buildingId AND zone_id = zoneId
    GROUP BY avatar_id, building_id, zone_id
  ) THEN
    INSERT INTO llubuildingcapture (avatar_id, building_id, zone_id, captures, exp)
    VALUES (avatarId, building_id, zone_id, 0, 0);
    out := 1;
  ELSE
    out := 0;
  END IF;
  RETURN out;
END;
$$ LANGUAGE plpgsql;

/*
The user of a character flags their login by setting the avatar_logged_in column on their account row entry to the unique character id.
This causes new session data to be written, starting with the selection of a new session index.
This marks a new play session for this character.
 */
CREATE OR REPLACE FUNCTION fn_account_newSession()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE sessionId Int;
DECLARE oldSessionId Int;
BEGIN
  avatarId := NEW.avatar_logged_in;
  oldSessionId := proc_sessionnumber_test(avatarId);
  sessionId := proc_sessionnumber_initAndOrIncreasePerHour(avatarId);
  IF (sessionId > oldSessionId) THEN
    BEGIN
      UPDATE account
      SET session_id = sessionId
      WHERE id = OLD.id;
      INSERT INTO kdasession (avatar_id, session_id)
      VALUES (avatarId, sessionId);
    END;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_account_newSession
AFTER UPDATE
OF avatar_logged_in
ON account
FOR EACH ROW
WHEN (OLD.avatar_logged_in = 0 AND NEW.avatar_logged_in > 0)
EXECUTE FUNCTION fn_account_newSession();

/*
A kill activity causes three major updates:
a character that represents the killer has their kill count updated/increased,
a character that represents the victim has their death count updated/increased, and
the weapon that was used in the activity has the kills count for the killer updated/increased.
Various other triggers are related to each of these updates.
*/
CREATE OR REPLACE FUNCTION fn_killactivity_updateRelatedStats()
RETURNS TRIGGER
AS
$$
DECLARE killerSessionId Int;
DECLARE victimSessionId Int;
DECLARE killerId Int;
DECLARE victimId Int;
DECLARE weaponId Int;
DECLARE out integer;
BEGIN
  killerId := NEW.killer_id;
  victimId := NEW.victim_id;
  weaponId := NEW.weapon_id;
  killerSessionId := proc_sessionnumber_get(killerId);
  victimSessionId := proc_sessionnumber_get(victimId);
  BEGIN
    UPDATE kdasession
    SET kills = kills + 1
    WHERE avatar_id = killerId AND session_id = killerSessionId;
  END;
  BEGIN
    UPDATE kdasession
    SET deaths = deaths + 1
    WHERE avatar_id = victimId AND session_id = victimSessionId;
  END;
  BEGIN
    out := proc_weaponstatsession_addEntryIfNoneWithSessionId(killerId, weaponId, killerSessionId);
    UPDATE weaponstatsession
    SET kills = kills + 1
    WHERE avatar_id = killerId AND session_id = killerSessionId AND weapon_id = weaponId;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_killactivity_updateRelatedStats
AFTER INSERT
ON killactivity
FOR EACH ROW
EXECUTE FUNCTION fn_killactivity_updateRelatedStats();

/*
Before attempting to update the data in a character's session weapon statistics row entry, make certain the row entry already exists.
If an entry for a player's session weapon statistics will be updated, as per the above triggers, this will certainly be required.
Not necessary for insertion events because that will create the row entry anyway.
*/
CREATE OR REPLACE FUNCTION fn_weaponstatsession_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
DECLARE out integer;
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  out := proc_weaponstatsession_addEntryIfNone(avatarId, weaponId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstatsession_addEntryIfNone
BEFORE UPDATE
ON weaponstatsession
FOR EACH ROW
EXECUTE FUNCTION fn_weaponstatsession_addEntryIfNone();

/*
Before attempting to update the data in a character's campaign KDA row entry, make certain the row entry already exists.
If an entry for a player's session KDA will be deleted, as per the above triggers, this will certainly be required.
Not necessary for insertion events because that will create the row entry anyway.
*/
CREATE OR REPLACE FUNCTION fn_kda_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE out integer;
BEGIN
  avatarId := NEW.avatar_id;
  out := proc_kda_addEntryIfNone(avatarId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_kda_addEntryIfNone
BEFORE UPDATE
ON kda
FOR EACH ROW
EXECUTE FUNCTION fn_kda_addEntryIfNone();

/*
Before attempting to update the data in a character's campaign weapon statistics row entry, make certain the row entry already exists.
If an entry for a player's session weapon statistics will be deleted, as per the above triggers, this will certainly be required.
Not necessary for insertion events because that will create the row entry anyway.
*/
CREATE OR REPLACE FUNCTION fn_weaponstat_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
DECLARE out integer;
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  out := proc_weaponstat_addEntryIfNone(avatarId, weaponId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstat_addEntryIfNone
BEFORE UPDATE
ON weaponstat
FOR EACH ROW
EXECUTE FUNCTION fn_weaponstat_addEntryIfNone();

/*
Before attempting to update the revival data in a character's session kda column,
set the session id to the current value then
manually update the column, overriding the process.
*/
CREATE OR REPLACE FUNCTION fn_kdasession_updateRevives()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE sessionId Int;
DECLARE newRevives Int;
BEGIN
  avatarId := NEW.avatar_id;
  sessionId := proc_sessionnumber_get(avatarId);
  newRevives := NEW.revives;
  if (newRevives > 0) THEN
    UPDATE kdasession
    SET revives = revives + newRevives
    WHERE avatar_id = avatarId AND session_id = sessionId;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_kdasession_updateRevives
BEFORE INSERT
ON kdasession
FOR EACH ROW
EXECUTE FUNCTION fn_kdasession_updateRevives();

/*
Upon deletion of row entries for a character's session KDA,
the values are copied over to the campaign KDA record for that character.
*/
CREATE OR REPLACE FUNCTION fn_kdasession_updateOnDelete()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE oldKills Int;
DECLARE oldDeaths Int;
DECLARE oldRevives Int;
DECLARE out integer;
BEGIN
  avatarId := OLD.avatar_id;
  oldKills := OLD.kills;
  oldDeaths := OLD.deaths;
  oldRevives := OLD.Revives;
  out := proc_kda_addEntryIfNone(avatarId);
  UPDATE kda
  SET kills = kills + oldKills,
    deaths = deaths + oldDeaths,
    revives = revives + oldRevives
  WHERE avatar_id = avatarId;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_kdasession_updateOnDelete
BEFORE DELETE
ON kdasession
FOR EACH ROW
EXECUTE FUNCTION fn_kdasession_updateOnDelete();

/*
Upon deletion of row entries for a character's session weapon stats,
the values are copied over to the campaign weapon stats record for that character.
*/
CREATE OR REPLACE FUNCTION fn_weaponstatsession_updateOnDelete()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
DECLARE oldKills Int;
DECLARE oldAssists Int;
DECLARE oldFired Int;
DECLARE oldLanded Int;
DECLARE out integer;
BEGIN
  avatarId := OLD.avatar_id;
  weaponId := OLD.weapon_id;
  oldKills := OLD.kills;
  oldAssists := OLD.assists;
  oldFired := OLD.shots_fired;
  oldLanded := OLD.shots_landed;
  out := proc_weaponstat_addEntryIfNone(avatarId, weaponId);
  UPDATE weaponstat
  SET kills = kills + oldKills,
    assists = assists + oldAssists,
    shots_fired = shots_fired + oldFired,
    shots_landed = shots_landed + oldLanded
  WHERE avatar_id = avatarId AND weapon_id = weaponId;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstatsession_updateOnDelete
BEFORE DELETE
ON weaponstatsession
FOR EACH ROW
EXECUTE FUNCTION fn_weaponstatsession_updateOnDelete();

/*
Before inserting a value into the weaponstatsession table to session id -1,
correct the session id to the most current session id,
and invalidate the attempted insertion at that -1 session id.
*/
CREATE OR REPLACE FUNCTION fn_weaponstatsession_beforeInsert()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
DECLARE sessionId Int;
DECLARE oldAssists Int;
DECLARE oldFired Int;
DECLARE oldLanded Int;
DECLARE out integer;
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  oldAssists := NEW.assists;
  oldFired := NEW.shots_fired;
  oldLanded := NEW.shots_landed;
  sessionId := proc_sessionnumber_get(avatarId);
  out := proc_weaponstatsession_addEntryIfNoneWithSessionId(avatarId, weaponId, sessionId);
  BEGIN
    UPDATE weaponstatsession
    SET
      assists = assists + oldAssists,
      shots_fired = shots_fired + oldFired,
      shots_landed = shots_landed + oldLanded
    WHERE avatar_id = avatarId AND session_id = sessionId AND weapon_id = weaponId;
  END;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstatsession_beforeInsert
BEFORE INSERT
ON weaponstatsession
FOR EACH ROW
WHEN (NEW.session_id = -1)
EXECUTE FUNCTION fn_weaponstatsession_beforeInsert();

/*
A kill assist activity causes a major update to weapon stats:
the weapon that was used in the activity has the kills count for the killer updated/increased.
*/
CREATE OR REPLACE FUNCTION fn_assistactivity_updateRelatedStats()
RETURNS TRIGGER
AS
$$
DECLARE killerSessionId Int;
DECLARE killerId Int;
DECLARE weaponId Int;
DECLARE out integer;
BEGIN
  killerId := NEW.killer_id;
  weaponId := NEW.weapon_id;
  killerSessionId := proc_sessionnumber_get(killerId);
  out := proc_weaponstatsession_addEntryIfNone(killerId, killerSessionId, weaponId);
  BEGIN
    UPDATE weaponstatsession
    SET assists = assists + 1
    WHERE avatar_id = killerId AND session_id = killerSessionId AND weapon_id = weaponId;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_assistactivity_updateRelatedStats
AFTER INSERT
ON killactivity
FOR EACH ROW
EXECUTE FUNCTION fn_assistactivity_updateRelatedStats();

/*
Upon deletion of row entries for a character's building capture table,
the values are copied over to a total capture record for that character.
This will sort the values into one of two tables, and,
for one table, it will add to either one column or to a different column.
*/
CREATE OR REPLACE FUNCTION fn_buildingcapture_updateOnDelete()
RETURNS TRIGGER
AS
$$
DECLARE oldAvatarId Int;
DECLARE oldZoneId Int;
DECLARE oldBuildingId Int;
DECLARE oldExp Int;
DECLARE oldType CHAR(3);
DECLARE out integer;
BEGIN
  oldAvatarId := NEW.avatar_id;
  oldZoneId := NEW.zone_id;
  oldBuildingId := NEW.building_id;
  oldExp := NEW.exp;
  oldType := NEW.exp_type;
  BEGIN
    IF (oldType LIKE "bep") THEN
      out := proc_expbuildingcapture_addEntryIfNone(avatarId, buildingId, zoneId);
      UPDATE expbuildingcapture
      SET bep = bep + oldExp
      WHERE avatar_id = oldAvatarId AND zone_id = oldZOneId AND building_id = oldBuildingId;
    ELSIF (oldType LIKE "cep") THEN
      out := proc_expbuildingcapture_addEntryIfNone(avatarId, buildingId, zoneId);
      UPDATE expbuildingcapture
      SET cep = cep + oldExp
      WHERE avatar_id = oldAvatarId AND zone_id = oldZOneId AND building_id = oldBuildingId;
    ELSIF (oldType LIKE "llu") THEN
      out := proc_llubuildingcapture_addEntryIfNone(avatarId, buildingId, zoneId);
      UPDATE llubuildingcapture
      SET exp = exp + oldExp
      WHERE avatar_id = oldAvatarId AND zone_id = oldZOneId AND building_id = oldBuildingId;
    END IF;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_buildingcapture_updateOnDelete
BEFORE DELETE
ON buildingcapture
FOR EACH ROW
EXECUTE FUNCTION fn_buildingcapture_updateOnDelete();

/*
If a new avatar is created, a corresponding progression debt entry is also created by default.
*/
CREATE OR REPLACE FUNCTION fn_avatar_addDebtEntry()
RETURNS TRIGGER
AS
$$
DECLARE newAvatarId Int;
BEGIN
  newAvatarId := NEW.avatar_id;
  BEGIN
    INSERT INTO progressiondebt (avatar_id)
    VALUES (newAvatarId);
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_avatar_addDebtEntry
AFTER INSERT
ON avatar
FOR EACH ROW
EXECUTE FUNCTION fn_avatar_addDebtEntry();
