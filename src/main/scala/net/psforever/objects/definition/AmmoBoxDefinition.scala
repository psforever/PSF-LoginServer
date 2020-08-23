// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.AmmoBoxConverter
import net.psforever.objects.equipment.Ammo

class AmmoBoxDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  import net.psforever.objects.equipment.EquipmentSize
  private val ammoType: Ammo.Value = Ammo(objectId) //let throw NoSuchElementException
  private var capacity: Int        = 1
  Name = "ammo box"
  Size = EquipmentSize.Inventory
  Packet = AmmoBoxDefinition.converter

  def AmmoType: Ammo.Value = ammoType

  def Capacity: Int = capacity

  def Capacity_=(capacity: Int): Int = {
    this.capacity = capacity
    Capacity
  }
}

object AmmoBoxDefinition {
  private val converter = new AmmoBoxConverter()

  def apply(objectId: Int): AmmoBoxDefinition = {
    new AmmoBoxDefinition(objectId)
  }

  def apply(ammoType: Ammo.Value): AmmoBoxDefinition = {
    new AmmoBoxDefinition(ammoType.id)
  }
}
