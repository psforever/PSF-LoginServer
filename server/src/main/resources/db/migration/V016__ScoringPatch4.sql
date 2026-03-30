-- Fix scoring functions and triggers
-- Cleanup

-- fix trigger function (OLD for DELETE actions)
-- fix capture counter
-- fix functions by using correct variable names instead of out of scope table columns
-- improve functions to use index with ON CONFLICT
-- cleanup by removing return values,

CREATE OR REPLACE FUNCTION fn_buildingcapture_updateOnDelete()
    RETURNS TRIGGER
AS
$$
DECLARE
    oldAvatarId   INT;
    oldZoneId     INT;
    oldBuildingId INT;
    oldExp        INT;
    oldType       TEXT;
BEGIN
    oldAvatarId   := OLD.avatar_id;
    oldZoneId     := OLD.zone_id;
    oldBuildingId := OLD.building_id;
    oldExp        := OLD.exp;
    oldType       := OLD.exp_type;

    IF (oldType = 'bep') THEN
        PERFORM proc_expbuildingcapture_addEntryIfNone(oldAvatarId, oldBuildingId, oldZoneId);
        UPDATE expbuildingcapture
        SET
            captures = captures + 1,
            bep = bep + oldExp
        WHERE avatar_id = oldAvatarId AND zone_id = oldZoneId AND building_id = oldBuildingId;

    ELSIF (oldType = 'cep') THEN
        PERFORM proc_expbuildingcapture_addEntryIfNone(oldAvatarId, oldBuildingId, oldZoneId);
        UPDATE expbuildingcapture
        SET
            captures = captures + 1,
            cep = cep + oldExp
        WHERE avatar_id = oldAvatarId AND zone_id = oldZoneId AND building_id = oldBuildingId;

    ELSIF (oldType = 'llu') THEN
        PERFORM proc_llubuildingcapture_addEntryIfNone(oldAvatarId, oldBuildingId, oldZoneId);
        UPDATE llubuildingcapture
        SET
            captures = captures + 1,
            exp = exp + oldExp
        WHERE avatar_id = oldAvatarId AND zone_id = oldZoneId AND building_id = oldBuildingId;
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION proc_expbuildingcapture_addEntryIfNone;
CREATE OR REPLACE FUNCTION proc_expbuildingcapture_addEntryIfNone
(avatarId INT, buildingId INT, zoneId INT)
    RETURNS VOID
AS
$$
BEGIN
    INSERT INTO expbuildingcapture (avatar_id, zone_id, building_id, captures, bep, cep)
    VALUES (avatarId, zoneId, buildingId, 0, 0, 0)
    ON CONFLICT (avatar_id, zone_id, building_id) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION proc_llubuildingcapture_addEntryIfNone;
CREATE OR REPLACE FUNCTION proc_llubuildingcapture_addEntryIfNone
(avatarId INT, buildingId INT, zoneId INT)
    RETURNS VOID
AS
$$
BEGIN
    INSERT INTO llubuildingcapture (avatar_id, zone_id, building_id, captures, exp)
    VALUES (avatarId, zoneId, buildingId, 0, 0)
    ON CONFLICT (avatar_id, zone_id, building_id) DO NOTHING;
END;
$$ LANGUAGE plpgsql;
