/* Original: V008__Scoring.sql */
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
  out := proc_weaponstatsession_addEntryIfNoneWithSessionId(killerId, killerSessionId, weaponId);
  BEGIN
    UPDATE weaponstatsession
    SET assists = assists + 1
    WHERE avatar_id = killerId AND session_id = killerSessionId AND weapon_id = weaponId;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/* New */
ALTER TABLE "progressiondebt"
ADD COLUMN IF NOT EXISTS "max_experience" INT NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS "enroll_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS "clear_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

/*
Upon indoctrinating a player into the progression system,
update the peak experience for the battle rank for future reference
and record when the player asked for this enhanced rank promotion.
*/
CREATE OR REPLACE FUNCTION fn_progressiondebt_updateEnrollment()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE oldExp Int;
DECLARE newExp Int;
BEGIN
  avatarId := NEW.avatar_id;
  newExp := NEW.experience;
  oldExp := OLD.experience;
  BEGIN
    IF (oldExp = 0 AND newExp > 0) THEN
      UPDATE progressiondebt
      SET experience = newExp, max_experience = newExp, enroll_time = CURRENT_TIMESTAMP
      WHERE avatar_id = avatarId;
    END IF;
  END;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER psf_progressiondebt_updateEnrollment
BEFORE UPDATE
ON progressiondebt
FOR EACH ROW
EXECUTE PROCEDURE fn_progressiondebt_updateEnrollment();

/*
Upon unlisting a player from the progression system,
update the time when the player has completed his tensure.
*/
CREATE OR REPLACE FUNCTION fn_progressiondebt_updateClearTime()
RETURNS TRIGGER
AS
$$
DECLARE avatarId Int;
DECLARE oldExp Int;
DECLARE newExp Int;
DECLARE newMaxExp Int;
BEGIN
  avatarId := NEW.avatar_id;
  newExp := NEW.experience;
  oldExp := OLD.experience;
  newMaxExp := NEW.max_experience;
  BEGIN
    IF (oldExp > newExp AND newExp = 0) THEN
      UPDATE progressiondebt
      SET experience = 0, max_experience = newMaxExp, clear_time = CURRENT_TIMESTAMP
      WHERE avatar_id = avatarId;
    END IF;
  END;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER psf_progressiondebt_updateClearTime
BEFORE UPDATE
ON progressiondebt
FOR EACH ROW
EXECUTE PROCEDURE fn_progressiondebt_updateClearTime();
