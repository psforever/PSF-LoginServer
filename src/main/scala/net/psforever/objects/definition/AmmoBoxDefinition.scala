// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.AmmoBoxConverter
import net.psforever.objects.equipment.{Ammo, EquipmentSize}

class AmmoBoxDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  Name = "ammo_box"
  Size = EquipmentSize.Inventory
  Packet = AmmoBoxDefinition.converter
  private val ammoType: Ammo.Value = Ammo(objectId) //let throw NoSuchElementException
  private var capacity: Int        = 1
  var repairAmount: Float          = 0
  registerAs = "ammo"


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
