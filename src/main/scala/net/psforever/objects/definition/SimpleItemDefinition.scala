// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.equipment.SItem

class SimpleItemDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  import net.psforever.objects.equipment.EquipmentSize
  SItem(objectId) //let throw NoSuchElementException
  Name = "simple_item"
  Size = EquipmentSize.Pistol //all items
  registerAs = "items"
}

object SimpleItemDefinition {
  def apply(objectId: Int): SimpleItemDefinition = {
    new SimpleItemDefinition(objectId)
  }

  def apply(simpItem: SItem.Value): SimpleItemDefinition = {
    new SimpleItemDefinition(simpItem.id)
  }
}
