// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

sealed case class BlockMapEntry(map: BlockMap, coords: Vector3, rangeX: Float, rangeY: Float, sectors: Set[Int])

/**
  * An game object that can be represented on a blockmap.
  * The only requirement is that the entity can position itself in a zone's coordinate space.
  * @see `BlockMap`
  * @see `WorldEntity`
  */
trait BlockMapEntity
  extends WorldEntity {
  /** internal data regarding an active representation on a blockmap */
  private var _blockMapEntry: Option[BlockMapEntry] = None
  /** the function that allows for updates of the internal data */
  private var _updateBlockMapEntryFunc: (BlockMapEntity, Vector3) => Boolean = BlockMapEntity.doNotUpdateBlockMap

  /** internal data regarding an active representation on a blockmap */
  def blockMapEntry: Option[BlockMapEntry] = _blockMapEntry
  /** internal data regarding an active representation on a blockmap */
  def blockMapEntry_=(entry: Option[BlockMapEntry]): Option[BlockMapEntry] = {
    entry match {
      case None =>
        _updateBlockMapEntryFunc = BlockMapEntity.doNotUpdateBlockMap
        _blockMapEntry = None
      case Some(_) =>
        _updateBlockMapEntryFunc = BlockMapEntity.updateBlockMap
        _blockMapEntry = entry
    }
    entry
  }

  /**
    * Buckets in the blockmap are called "sectors".
    * Find the sectors in a given blockmap in which the entity would be represented within a given range.
    * @param zone what region the blockmap represents
    * @param range the custom distance from the central sector along the major axes
    * @return a conglomerate sector which lists all of the entities in the allocated sector(s)
    */
  def sector(zone: Zone, range: Float): SectorPopulation = {
    zone.blockMap.sector(
      //TODO same zone check?
      _blockMapEntry match {
        case Some(entry) => entry.coords
        case None => Position
      },
      range
    )
  }

  /**
    * Update the internal data's known coordinate position without changing representation on whatever blockmap.
    * Has the potential to cause major issues with the blockmap if used without external checks.
    * @param newCoords the coordinate position
    * @return `true`, if the coordinates were updated;
    *        `false`, otherwise
    */
  def updateBlockMapEntry(newCoords: Vector3): Boolean = _updateBlockMapEntryFunc(this, newCoords)
}

object BlockMapEntity {
  /**
    * Overloaded constructor that uses a single range to construct a block map entry.
    * @param coords the absolute game world coordinates
    * @param range the distance outwards from the game world coordinates along the major axes
    * @param sectors the indices of sectors on the blockmap
    * @return a `BlockMapEntry` entity
    */
  def apply(blocks: BlockMap, coords: Vector3, range: Float, sectors: Set[Int]): BlockMapEntry =
    BlockMapEntry(blocks, coords, range, range, sectors)

  /**
    * The entity is currently excluded from being represented on a blockmap structure.
    * There is no need to update.
    * @param target the entity on the blockmap
    * @param newCoords the world coordinates of the entity, the position to which it is moving / being moved
    * @return always `false`; we're not updating the entry
    */
  private def doNotUpdateBlockMap(target: BlockMapEntity, newCoords: Vector3): Boolean = false

  /**
    * Re-using other data from the entry,
    * update the data of the target entity's internal understanding of where it is represented on a blockmap.
    * Act as if the sector and the range that is encompassed never change,
    * though the game world coordinates of the entity have been changed.
    * (The range would probably not have changed in any case.
    * To properly update the range, perform a proper update.)
    * @param target the entity on the blockmap
    * @param newCoords the world coordinates of the entity, the position to which it is moving / being moved
    * @return `true`, if we are updating this entry; `false`, otherwsie
    */
  private def updateBlockMap(target: BlockMapEntity, newCoords: Vector3): Boolean = {
    target.blockMapEntry match {
      case Some(oldEntry) =>
        target.blockMapEntry = Some(
          BlockMapEntry(oldEntry.map, newCoords, oldEntry.rangeX, oldEntry.rangeY, oldEntry.sectors)
        )
        true
      case None =>
        false
    }
  }
}
