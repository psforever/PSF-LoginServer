// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.AmmoBoxConverter
import net.psforever.objects.equipment.Ammo

class AmmoBoxDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  import net.psforever.objects.equipment.EquipmentSize
  private val ammoType : Ammo.Value = Ammo(objectId) //let throw NoSuchElementException
  private var capacity : Int = 1
  private var damage0 : Int = 0
  private var damage1 : Int = 0
  private var damage2 : Int = 0
  private var damage3 : Int = 0
  private var damage4 : Int = 0
  Name = "ammo box"
  Size = EquipmentSize.Inventory
  Packet = AmmoBoxDefinition.converter

  def AmmoType : Ammo.Value = ammoType

  def Capacity : Int = capacity

  def Capacity_=(capacity : Int) : Int = {
    this.capacity = capacity
    Capacity
  }

  def Damage0 : Int = damage0
  def Damage0_=(damage : Int) : Int = {
    this.damage0 = damage
    Damage0
  }

  def Damage1 : Int = damage1
  def Damage1_=(damage : Int) : Int = {
    this.damage1 = damage
    Damage1
  }

  def Damage2 : Int = damage2
  def Damage2_=(damage : Int) : Int = {
    this.damage2 = damage
    Damage2
  }

  def Damage3 : Int = damage3
  def Damage3_=(damage : Int) : Int = {
    this.damage3 = damage
    Damage3
  }

  def Damage4 : Int = damage4
  def Damage4_=(damage : Int) : Int = {
    this.damage4 = damage
    Damage4
  }

}

object AmmoBoxDefinition {
  private val converter = new AmmoBoxConverter()

  def apply(objectId: Int) : AmmoBoxDefinition = {
    new AmmoBoxDefinition(objectId)
  }

  def apply(ammoType : Ammo.Value) : AmmoBoxDefinition = {
    new AmmoBoxDefinition(ammoType.id)
  }
}
