// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec

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

  override def Slot(slot : Int) : EquipmentSlot = {
    if(inventory.Offset <= slot && slot <= inventory.LastIndex) {
      inventory.Slot(slot)
    }
    else {
      OffhandEquipmentSlot.BlockedSlot
    }
  }


  def Fit(obj : Equipment) : Option[Int] = inventory.Fit(obj.Definition.Tile)

  def Find(guid : PlanetSideGUID) : Option[Int] = {
    findInInventory(inventory.Items.values.iterator, guid) match {
      case Some(index) =>
        Some(index)
      case None =>
        None
    }
  }

  @tailrec private def findInInventory(iter : Iterator[InventoryItem], guid : PlanetSideGUID) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val item = iter.next
      if(item.obj.GUID == guid) {
        Some(item.start)
      }
      else {
        findInInventory(iter, guid)
      }
    }
  }

  def Definition : EquipmentDefinition = GlobalDefinitions.locker_container
}

object LockerContainer {
  def apply() : LockerContainer = {
    new LockerContainer()
  }
}
