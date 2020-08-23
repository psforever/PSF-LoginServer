// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.InventoryTile

/**
  * The definition for any piece of `Equipment`.
  * @param objectId the object's identifier number
  */
abstract class EquipmentDefinition(objectId: Int) extends ObjectDefinition(objectId) {

  /** the size of the item when placed in an EquipmentSlot / holster / mounting */
  private var size: EquipmentSize.Value = EquipmentSize.Blocked

  /** the size of the item when placed in the grid inventory space */
  private var tile: InventoryTile = InventoryTile.Tile11

  /** a correction for the z-coordinate for some dropped items to avoid sinking into the ground */
  private var dropOffset: Float = 0f

  def Size: EquipmentSize.Value = size

  def Size_=(newSize: EquipmentSize.Value): EquipmentSize.Value = {
    size = newSize
    Size
  }

  def Tile: InventoryTile = tile

  def Tile_=(newTile: InventoryTile): InventoryTile = {
    tile = newTile
    Tile
  }

  def DropOffset: Float = dropOffset

  def DropOffset(offset: Float): Float = {
    dropOffset = offset
    DropOffset
  }
}
