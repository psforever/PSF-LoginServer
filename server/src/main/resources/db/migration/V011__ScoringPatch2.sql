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