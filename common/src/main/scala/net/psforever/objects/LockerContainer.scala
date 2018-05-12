// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}

/**
  * The companion of a `Locker` that is carried with a player
  * masquerading as their sixth `EquipmentSlot` object and a sub-inventory item.
  * The `Player` class refers to it as the "fifth slot" as its permanent slot number is encoded as `0x85`.
  * The inventory of this object is accessed using a game world `Locker` object (`mb_locker`).
  */
class LockerContainer extends Equipment with Container {
  private val inventory = GridInventory(30, 20)

  def Inventory : GridInventory = inventory

  def VisibleSlots : Set[Int] = Set.empty[Int]

  def Definition : EquipmentDefinition = GlobalDefinitions.locker_container
}

object LockerContainer {
  def apply() : LockerContainer = {
    new LockerContainer()
  }
}
