-- Outfit MOTD column size fix
--
-- The client's outfit message-of-the-day editor clamps its input/output to 256 wide characters.
ALTER TABLE outfit
    ALTER COLUMN "motd" TYPE VARCHAR(256);
