// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.definition.converter.LockerContainerConverter
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import net.psforever.objects.inventory.GridInventory

class LockerContainer extends Equipment {
  private val inventory = GridInventory() //?

  def Inventory : GridInventory = inventory

  def Fit(obj : Equipment) : Option[Int] = inventory.Fit(obj.Definition.Tile)

  def Definition : EquipmentDefinition = new EquipmentDefinition(456) {
    Name = "locker container"
    Size = EquipmentSize.Inventory
    Packet = new LockerContainerConverter()
  }
}

object LockerContainer {
  def apply() : LockerContainer = {
    new LockerContainer()
  }

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID) : LockerContainer = {
    val obj = new LockerContainer()
    obj.GUID = guid
    obj
  }
}
