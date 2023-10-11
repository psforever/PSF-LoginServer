/* Original: V008__Scoring.sql */
CREATE OR REPLACE FUNCTION fn_avatar_addDebtEntry()
RETURNS TRIGGER
AS
$$
DECLARE newAvatarId Int;
BEGIN
  newAvatarId := NEW.id;
  BEGIN
    INSERT INTO progressiondebt (avatar_id)
    VALUES (newAvatarId);
  END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
