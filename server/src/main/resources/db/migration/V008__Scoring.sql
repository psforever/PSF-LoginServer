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
  "victim_id" INT NOT NULL REFERENCES avatar (id),
  "killer_id" INT NOT NULL REFERENCES avatar (id),
  "victim_exosuit" SMALLINT NOT NULL,
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
  "assists" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id)
);

CREATE TABLE IF NOT EXISTS "kdasession" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "session_id" INT NOT NULL,
  "kills" INT NOT NULL DEFAULT 0,
  "deaths" INT NOT NULL DEFAULT 0,
  "assists" INT NOT NULL DEFAULT 0,
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

CREATE VIEW kdacampaign AS (
  SELECT
    session.avatar_id,
    SUM(session.kills) AS kills,
    SUM(session.deaths) AS deaths,
    SUM(session.assists) AS assists,
    COUNT(session.avatar_id) AS numberOfSessions
  FROM (
    SELECT avatar_id, session_id, kills, deaths, assists
    FROM kdasession
    UNION ALL
    SELECT avatar_id, 0, kills, deaths, assists
    FROM kda
  ) AS session
  LEFT JOIN kda
  ON kda.avatar_id = session.avatar_id
  GROUP BY session.avatar_id
);

CREATE VIEW weaponstatcampaign AS (
  SELECT
    weaponstat.avatar_id,
    weaponstat.weapon_id,
    SUM(session.shots_fired) AS shots_fired,
    SUM(session.shots_landed) AS shots_landed,
    SUM(session.kills) AS kills,
    SUM(session.assists) AS assists,
    COUNT(session.session_id) AS numberOfSessions
  FROM (
    SELECT avatar_id, weapon_id, session_id, shots_fired, shots_landed, kills, assists
    FROM weaponstatsession
    UNION ALL
    SELECT avatar_id, weapon_id, 0, shots_fired, shots_landed, kills, assists
    FROM weaponstat
  ) AS session
  LEFT JOIN weaponstat
  ON weaponstat.avatar_id = session.avatar_id
  GROUP BY weaponstat.avatar_id, weaponstat.weapon_id
);

/*
Procedure for initializing and increasing the session number.
Always indexes a new session.
*/
CREATE OR REPLACE PROCEDURE proc_sessionnumber_initAndOrIncrease
(avatarId IN Int, number OUT Int)
AS
$$
BEGIN
  SELECT (MAX(session_id,0)+1) INTO number
  FROM sessionnumber
  WHERE avatar_id = avatarId;
  INSERT INTO sessionnumber
  VALUES (avatarId, number);
END;
$$ LANGUAGE plpgsql;

/*
Procedure for initializing and increasing the session number.
Index for a new session only if last session was created more than one hour ago.
*/
CREATE OR REPLACE PROCEDURE proc_sessionnumber_initAndOrIncreasePerHour
(avatarId IN Int, number OUT Int, nextNumber OUT Int)
AS
$$
DECLARE time TIMESTAMP;
BEGIN
  SELECT MAX(session_id,0) INTO number
  FROM sessionnumber
  WHERE avatar_id = avatarId;
  SELECT timestamp INTO time
  FROM sessionnumber
  WHERE avatar_id = avatarId AND session_id = number;
  IF (CAST(CURRENT_TIMESTAMP AS FLOAT) > CAST(DATE_ADD('hour', 1, DATE_TRUNC('hour', time)) AS FLOAT)) THEN
    nextNumber := number + 1;
    INSERT INTO sessionnumber
    VALUES (avatarId, number);
  ELSE
    nextNumber := number;
    UPDATE sessionnumber
    SET timestamp = CURRENT_TIMESTAMP
    WHERE avatar_id = avatarId AND session_id = number;
  END IF;
END;
$$ LANGUAGE plpgsql;

/*
Procedure for accessing any existing session number.
Actually polls the previous session number from the account table.
*/
CREATE OR REPLACE PROCEDURE proc_sessionnumber_get
(avatarId IN Int, number OUT Int)
AS
$$
BEGIN
  SELECT COALESCE(session_id,0) INTO number
  FROM account
  WHERE avatar_logged_in = avatarId;
END;
$$ LANGUAGE plpgsql;

/*
Procedures for ensuring that row entries can be found in specified tables before some other DML operation updates those row entries.
*/
CREATE OR REPLACE PROCEDURE proc_kda_addEntryIfNone
(avatarId IN Int)
AS
$$
BEGIN
  IF EXISTS(
    SELECT *
    FROM kda
    WHERE avatar_id = avatarId
    HAVING COUNT(*) = 0) THEN
      INSERT INTO kda (avatar_id)
      VALUES (avatarId);
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE proc_weaponstat_addEntryIfNone
(avatarId IN Int, weaponId IN Int)
AS
$$
DECLARE sessionId Int;
BEGIN
  SELECT proc_sessionnumber_get(avatarId, sessionId);
  IF EXISTS(
    SELECT *
    FROM weaponstat
    WHERE avatar_id = avatarId AND weapon_id = weaponId
    HAVING COUNT(*) = 0) THEN
      INSERT INTO weaponstat (avatar_id, session_id, weapon_id)
      VALUES (avatarId, sessionId, weaponId);
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE proc_weaponstatsession_addEntryIfNone
(avatarId IN Int, weaponId IN Int)
AS
$$
DECLARE sessionId Int;
BEGIN
  SELECT proc_sessionnumber_get(avatarId, sessionId);
  IF EXISTS(
    SELECT *
    FROM weaponstatsession
    WHERE avatar_id = avatarId AND weapon_id = weaponId
    HAVING COUNT(*) = 0) THEN
      INSERT INTO weaponstatsession (avatar_id, session_id, weapon_id)
      VALUES (avatarId, sessionId, weaponId);
  END IF;
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
  SELECT proc_sessionnumber_initAndOrIncreasePerHour(avatarId, oldSessionId, sessionId);
  IF (sessionId > oldSessionId) THEN
    BEGIN
      UPDATE account
      SET sessionId = sessionId
      WHERE id = OLD.id;
      INSERT INTO kdasession (avatar_id, session_id)
      VALUES (avatarId, sessionId);
    END;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_account_newSession
BEFORE UPDATE
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
BEGIN
  killerId := NEW.killer_id;
  victimId := NEW.victim_id;
  weaponId := NEW.weapon_id;
  SELECT proc_sessionnumber_get(killerId, killerSessionId);
  SELECT proc_sessionnumber_get(victimId, victimSessionId);
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
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  SELECT proc_weaponstatsession_addEntryIfNone(avatarId, weaponId);
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
BEGIN
  avatarId := NEW.avatar_id;
  SELECT proc_kda_addEntryIfNone(avatarId);
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
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  SELECT proc_weaponstat_addEntryIfNone(avatarId, weaponId);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstat_addEntryIfNone
BEFORE UPDATE
ON weaponstat
FOR EACH ROW
EXECUTE FUNCTION fn_weaponstat_addEntryIfNone();

/*
Upon deletion of row entries for a character's session KDA,
the values are copied over to the campaign KDA record for that character.
This will fire mainly when called by the trigger for login (above).
*/
CREATE OR REPLACE FUNCTION fn_kdasession_updateOnDelete()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE oldKills Int;
DECLARE oldDeaths Int;
DECLARE oldAssists Int;
BEGIN
  avatarId := OLD.avatar_id;
  oldKills := OLD.kills;
  oldDeaths := OLD.deaths;
  oldAssists := OLD.assists;
  UPDATE kda
  SET kills = kills + oldKills,
    deaths = deaths + oldDeaths,
    assists = assists + oldAssists
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
This will fire mainly when called by the trigger for login (above).
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
BEGIN
  avatarId := OLD.avatar_id;
  weaponId := OLD.weapon_id;
  oldKills := OLD.kills;
  oldAssists := OLD.assists;
  oldFired := OLD.shots_fired;
  oldLanded := OLD.shots_landed;
  UPDATE weaponstat
  SET kills = kills + oldKills,
    assists = assists + oldAssists,
    shots_fired = shots_fired + oldFired,
    shots_landed = shots_landed + oldLanded
  WHERE avatar_id = avatarId AND weapon_id = weaponId;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER psf_weaponstatsession_updateOnDelete
BEFORE DELETE
ON weaponstatsession
FOR EACH ROW
EXECUTE FUNCTION fn_weaponstatsession_updateOnDelete();
