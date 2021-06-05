// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.blockmap

import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

sealed case class BlockMapEntry(coords: Vector3, range: Float, sectors: Set[Int])

trait BlockMapEntity
  extends WorldEntity {
  private var _blockMapEntry: Option[BlockMapEntry] = None
  private var _updateBlockMapEntryFunc: (BlockMapEntity, Vector3) => Boolean = BlockMapEntity.doNotUpdateBlockMap

  def blockMapEntry: Option[BlockMapEntry] = _blockMapEntry

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

  def updateBlockMapEntry(newCoords: Vector3): Boolean = _updateBlockMapEntryFunc(this, newCoords)
}

object BlockMapEntity {
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
    * @return always `true`; we are updating this entry
    */
  private def updateBlockMap(target: BlockMapEntity, newCoords: Vector3): Boolean = {
    val oldEntry = target.blockMapEntry.get
    target.blockMapEntry = Some(BlockMapEntry(newCoords, oldEntry.range, oldEntry.sectors))
    true
  }
}
