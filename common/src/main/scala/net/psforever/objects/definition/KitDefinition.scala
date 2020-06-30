// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.KitConverter
import net.psforever.objects.equipment.Kits

/**
  * The definition for a personal one-time-use recovery item.
  * @param objectId the object's identifier number
  */
class KitDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  import net.psforever.objects.equipment.EquipmentSize
  import net.psforever.objects.inventory.InventoryTile
  Kits(objectId) //let throw NoSuchElementException
  Size = EquipmentSize.Inventory
  Tile = InventoryTile.Tile42
  Name = "kit"
  Packet = KitDefinition.converter
}

object KitDefinition {
  private val converter = new KitConverter()

  def apply(objectId: Int): KitDefinition = {
    new KitDefinition(objectId)
  }

  def apply(kit: Kits.Value): KitDefinition = {
    new KitDefinition(kit.id)
  }
}
