/* Original: V008__Scoring.sql, overrode by V011__ScoringPatch2.sql */
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
  out := proc_weaponstatsession_addEntryIfNoneWithSessionId(killerId, weaponId, killerSessionId);
  BEGIN
    UPDATE weaponstatsession
    SET assists = assists + 1
    WHERE avatar_id = killerId AND weapon_id = weaponId AND session_id = killerSessionId;
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/* New */
CREATE TABLE IF NOT EXISTS "avatarmodepermission" (
  "avatar_id" INT NOT NULL REFERENCES avatar (id),
  "can_spectate" BOOLEAN NOT NULL DEFAULT FALSE,
  "can_gm" BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE(avatar_id)
);
