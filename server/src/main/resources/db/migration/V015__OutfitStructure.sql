-- Tables for Outfits
--
-- Includes Outfit, OutfitMember and OutfitPoints

/*

This migration allows for the storage of all outfit relevant data.

Outfit
- each outfit has one entry in the outfit table
- each name is unique and ranks are inlined (static 1:n, join unnecessary)
- faction is limited to 0,1,2,3
- decal is limited to 0 through 26 (inclusive)

OutfitMember
- each avatar can at most be a member in one outfit
- each outfit can only have one rank 7 (leader) member
- rank is limited to 0 through 7 (inclusive)
- there is a "quick access" index on outfit and avatar for rank 7 (leader)

OutfitPoint
- each (outfit, avatar) combination can only exist once
- a (outfit, NULL) combination can not be limited yet (not a big deal)
- deleting a avatar will have his points remain as (outfit, NULL)
- leaving an outfit will have the points remain as (outfit, NULL)

*/

-- OUTFIT

CREATE TABLE outfit (
    "id" SERIAL PRIMARY KEY,
    "created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" BOOLEAN NOT NULL DEFAULT FALSE, -- allow for recovery of accidentially deleted outfits
    "faction" SMALLINT NOT NULL,
    "owner_id" INTEGER NOT NULL,
    "decal" SMALLINT NOT NULL DEFAULT 0,
    "name" VARCHAR(32) NOT NULL,
    "motd" VARCHAR(255) NULL,
    "rank0" VARCHAR(32) NULL, -- Non-Officer Rank 1
    "rank1" VARCHAR(32) NULL,
    "rank2" VARCHAR(32) NULL,
    "rank3" VARCHAR(32) NULL, -- Non-Officer Rank 4
    "rank4" VARCHAR(32) NULL, -- Fourth In Command
    "rank5" VARCHAR(32) NULL,
    "rank6" VARCHAR(32) NULL, -- Second In Command
    "rank7" VARCHAR(32) NULL, -- Outfit Leader

    CONSTRAINT "outfit_faction_check" CHECK("faction" BETWEEN 0 AND 3), -- allowed faction IDs
    CONSTRAINT "outfit_decal_check" CHECK("decal" BETWEEN 0 AND 26),     -- allowed decal IDs

    CONSTRAINT "outfit_owner_id_avatar_id_fkey" FOREIGN KEY ("owner_id") REFERENCES avatar ("id")
);

CREATE INDEX "outfit_created_brin_idx" ON "outfit" USING BRIN ("created"); -- super small, index for physically sequential data
CREATE INDEX "outfit_faction_deleted_idx" ON "outfit" ("faction", "deleted"); -- optimize index for search: SELECT * FROM "outfit" WHERE "faction" = ? AND "deleted" = false;

-- OUTFITMEMBER

CREATE TABLE outfitmember (
    "id" BIGSERIAL PRIMARY KEY,
    "created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "outfit_id" INTEGER NOT NULL,
    "avatar_id" INTEGER NOT NULL,
    "rank" SMALLINT NOT NULL DEFAULT 0, -- lowest rank

    CONSTRAINT "outfitmember_rank_check" CHECK("rank" BETWEEN 0 AND 7), -- allowed ranks

    CONSTRAINT "outfitmember_outfit_id_outfit_id_fkey" FOREIGN KEY ("outfit_id") REFERENCES outfit ("id") ON DELETE CASCADE,
    CONSTRAINT "outfitmember_avatar_id_avatar_id_fkey" FOREIGN KEY ("avatar_id") REFERENCES avatar ("id") ON DELETE RESTRICT
);

CREATE INDEX "outfitmember_outfit_id_idx" ON "outfitmember" ("outfit_id"); -- FK index
CREATE UNIQUE INDEX "outfitmember_avatar_id_unique" ON "outfitmember" ("avatar_id"); -- FK index, enforce one outfit per avatar
CREATE UNIQUE INDEX "outfitmember_outfit_id_rank_partial_leader_unique" ON "outfitmember" ("outfit_id", "rank") WHERE "rank" = 7; -- quick access to outfit leader and ony one leader per outfit

-- OUTFITPOINT

CREATE TABLE outfitpoint (
    "id" BIGSERIAL PRIMARY KEY,
    "outfit_id" INTEGER NOT NULL,
    "avatar_id" INTEGER NULL,
    "points" INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT "outfitpoint_points_check" CHECK ("points" >= 0),

    -- enforce unique combinations (left side index)
    CONSTRAINT "outfitpoint_outfit_avatar_unique_idx" UNIQUE ("outfit_id", "avatar_id"), -- UNIQUE NULLS NOT DISTINCT

    CONSTRAINT "outfitpoint_outfit_fkey" FOREIGN KEY ("outfit_id") REFERENCES outfit ("id") ON DELETE CASCADE, -- delete points of outfit when outfit is deleted
    CONSTRAINT "outfitpoint_avatar_fkey" FOREIGN KEY ("avatar_id") REFERENCES avatar ("id") ON DELETE SET NULL -- keep points for outfit when player is deleted
);

-- add right side index (avatar_id)
CREATE INDEX "outfitpoint_avatar_idx" ON "outfitpoint" ("avatar_id");

-- MATERIALIZED VIEW for OUTFITPOINT

CREATE MATERIALIZED VIEW outfitpoint_mv AS
    SELECT
        "outfit_id",
        SUM("points") as "points"
    FROM
        "outfitpoint"
    GROUP BY "outfit_id";

CREATE INDEX "outfitpoint_mv_outfit_id_idx" ON "outfitpoint_mv" ("outfit_id");
