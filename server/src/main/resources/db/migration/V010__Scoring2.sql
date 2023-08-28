/* changes to objects from V008__Scoring.sql */
ALTER TABLE killactivity
ADD COLUMN victim_mounted INT NOT NULL DEFAULT 0;

DROP PROCEDURE IF EXISTS proc_sessionnumber_initAndOrIncrease;

CREATE OR REPLACE PROCEDURE proc_sessionnumber_initAndOrIncreasePerHour
(avatarId IN Int, number OUT Int, nextNumber OUT Int)
AS
$$
DECLARE time TIMESTAMP;
BEGIN
  SELECT MAX(session_id) INTO number
  FROM sessionnumber
  WHERE avatar_id = avatarId;
  SELECT COALESCE(timestamp) INTO time
  FROM sessionnumber
  WHERE avatar_id = avatarId AND session_id = number;
  IF (time IS null) THEN
    number := 0;
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
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE proc_sessionnumber_get
(avatarId IN Int, number OUT Int)
AS
$$
DECLARE sessionId Int;
BEGIN
  SELECT COALESCE(session_id) INTO sessionId
  FROM account
  WHERE avatar_logged_in = avatarId;
  IF sessionId IS NULL THEN
    number := 0;
  ELSE
    number := sessionId;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE proc_weaponstat_addEntryIfNone
(avatarId IN Int, weaponId IN Int)
AS
$$
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, weapon_id
    FROM weaponstat
    WHERE avatar_id = avatarId AND weapon_id = weaponId
    GROUP BY avatar_id, weapon_id
  ) THEN
    INSERT INTO weaponstat (avatar_id, weapon_id)
    VALUES (avatarId, weaponId);
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE proc_weaponstatsession_addEntryIfNoneWithSessionId
(avatarId IN Int, sessionId IN Int, weaponId IN Int)
AS
$$
BEGIN
  IF NOT EXISTS(
    SELECT avatar_id, session_id, weapon_id
    FROM weaponstatsession
    WHERE avatar_id = avatarId AND weapon_id = weaponId AND session_id = sessionId
    GROUP BY avatar_id, session_id, weapon_id
  ) THEN
    INSERT INTO weaponstatsession (avatar_id, session_id, weapon_id)
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
  CALL proc_sessionnumber_get(avatarId, sessionId);
  CALL proc_weaponstatsession_addEntryIfNoneWithSessionId(avatarId, sessionId, weaponId);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_account_newSession()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE sessionId Int;
DECLARE oldSessionId Int;
BEGIN
  avatarId := NEW.avatar_logged_in;
  CALL proc_sessionnumber_initAndOrIncreasePerHour(avatarId, oldSessionId, sessionId);
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
  CALL proc_sessionnumber_get(killerId, killerSessionId);
  CALL proc_sessionnumber_get(victimId, victimSessionId);
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
    CALL proc_weaponstatsession_addEntryIfNoneWithSessionId(killerId, killerSessionId, weaponId);
    UPDATE weaponstatsession
    SET kills = kills + 1
    WHERE avatar_id = killerId AND session_id = killerSessionId AND weapon_id = weaponId;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_weaponstatsession_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  CALL proc_weaponstatsession_addEntryIfNone(avatarId, weaponId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_kda_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
BEGIN
  avatarId := NEW.avatar_id;
  CALL proc_kda_addEntryIfNone(avatarId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_weaponstat_addEntryIfNone()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE weaponId Int;
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  CALL proc_weaponstat_addEntryIfNone(avatarId, weaponId);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

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
  CALL proc_kda_addEntryIfNone(avatarId);
  UPDATE kda
  SET kills = kills + oldKills,
    deaths = deaths + oldDeaths,
    assists = assists + oldAssists
  WHERE avatar_id = avatarId;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

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
  CALL proc_weaponstat_addEntryIfNone(avatarId, weaponId);
  UPDATE weaponstat
  SET kills = kills + oldKills,
    assists = assists + oldAssists,
    shots_fired = shots_fired + oldFired,
    shots_landed = shots_landed + oldLanded
  WHERE avatar_id = avatarId AND weapon_id = weaponId;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

/* new objects */
CREATE TABLE IF NOT EXISTS machinedestroyed (
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

CREATE TABLE IF NOT EXISTS supportactivity (
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

CREATE TABLE IF NOT EXISTS respawnsession (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "session_id" INT NOT NULL,
  "respawn_count" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id, session_id)
);

CREATE TABLE IF NOT EXISTS respawn (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "respawn_count" INT NOT NULL DEFAULT 0,
  UNIQUE(avatar_id)
);

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
BEGIN
  avatarId := NEW.avatar_id;
  weaponId := NEW.weapon_id;
  oldAssists := NEW.assists;
  oldFired := NEW.shots_fired;
  oldLanded := NEW.shots_landed;
  CALL proc_sessionnumber_get(avatarId, sessionId);
  CALL proc_weaponstatsession_addEntryIfNoneWithSessionId(avatarId, sessionId, weaponId);
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
BEGIN
  killerId := NEW.killer_id;
  weaponId := NEW.weapon_id;
  SELECT proc_sessionnumber_get(killerId, killerSessionId);
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
