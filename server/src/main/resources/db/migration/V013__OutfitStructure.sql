-- Tables for Outfits
--
-- Includes Outfit, OutfitMember and OutfitPoints

/*

This migration (presumably) allows for the storage of all outfit relevant data (except titles).

Outfit
- each outfit has one entry in the outfit table
- each name is unique and ranks are inlined (static 1:n, join unnecessary)
- faction is limited to 0,1,2,3
- decal is limited to 1 through 26 (inclusive)

OutfitMember
- each avatar can at most be a member in one outfit
- each outfit can only have one rank 1 (leader) member
- rank is limited to 1 through 8 (inclusive)
- there is a "quick access" index on outfit and avatar for rank 1 (leader)

OutfitPoint
- each (outfit, avatar) combination can only exist once
- a (outfit, NULL) combination can not be limited yet (not a big deal)
- points is limited to >= 0
- deleating a avatar will have his points remain as (outfit, NULL)

*/

-- OUTFIT

CREATE TABLE outfit (
    id SERIAL PRIMARY KEY,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE, -- allow for recovery of accidentially deleted outfits
    faction_id SMALLINT NOT NULL,
    decal SMALLINT NULL,
    name TEXT NOT NULL UNIQUE,
    motd TEXT NULL,
    rank1 TEXT NULL, -- Outfit Leader
    rank2 TEXT NULL, -- Second In Command
    rank3 TEXT NULL,
    rank4 TEXT NULL, -- Fourth In Command
    rank5 TEXT NULL, -- Non-Officer Rank 4
    rank6 TEXT NULL,
    rank7 TEXT NULL,
    rank8 TEXT NULL, -- Non-Officer Rank 1

    CONSTRAINT "outfit_faction_check" CHECK(faction_id BETWEEN 0 AND 3), -- allowed faction IDs
    CONSTRAINT "outfit_decal_check" CHECK(decal BETWEEN 1 AND 26)        -- allowed decal IDs
);

CREATE INDEX "outfit_faction_idx" ON "outfit" (faction_id);
CREATE INDEX "outfit_created_brin_idx" ON "outfit" USING BRIN ("created"); -- super small, index for physically sequential data

-- OUTFITMEMBER

CREATE TABLE outfitmember (
    outfit_id INTEGER NOT NULL,
    avatar_id INTEGER NOT NULL,
    "rank" SMALLINT NOT NULL DEFAULT 8, -- lowest rank

    CONSTRAINT "outfitmember_rank_check" CHECK("rank" BETWEEN 1 AND 8), -- allowed ranks

    -- enforce unique, only one outfit per avatar
    CONSTRAINT "outfitmember_avatar_unique_idx" UNIQUE (avatar_id),

    CONSTRAINT "outfitmember_outfit_fkey" FOREIGN KEY (outfit_id) REFERENCES outfit(id) ON DELETE CASCADE,
    CONSTRAINT "outfitmember_avatar_fkey" FOREIGN KEY (avatar_id) REFERENCES avatar(id) ON DELETE CASCADE
);

CREATE INDEX "outfitmember_outfit_idx" ON "outfitmember" (outfit_id);
CREATE UNIQUE INDEX "outfitmember_outfit_partial_leader_idx" ON "outfitmember" (outfit_id, "rank") WHERE "rank" = 1; -- quick access to outfit leader and ony one leader per outfit

-- OUTFITPOINT

CREATE TABLE outfitpoint (
    outfit_id INTEGER NOT NULL,
    avatar_id INTEGER NULL,
    points INTEGER NOT NULL,

    CONSTRAINT "outfitpoint_points_check" CHECK (points >= 0),

    -- enforce unique combinations (left side index)
    CONSTRAINT "outfitpoint_outfit_avatar_unique_idx" UNIQUE (outfit_id, avatar_id), -- UNIQUE NULLS NOT DISTINCT

    CONSTRAINT "outfitpoint_outfit_fkey" FOREIGN KEY (outfit_id) REFERENCES outfit(id) ON DELETE CASCADE, -- delete points of outfit when outfit is deleted
    CONSTRAINT "outfitpoint_avatar_fkey" FOREIGN KEY (avatar_id) REFERENCES avatar(id) ON DELETE SET NULL -- keep points for outfit when player is deleted
);

-- add right side index (avatar_id)
CREATE INDEX "outfitpoint_avatar_idx" ON "outfitpoint" (avatar_id);
