// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.Actor
import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.{Containable, PlanetSideServerObject}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

/**
  * The companion of a `Locker` that is carried with a player
  * masquerading as their sixth `EquipmentSlot` object and a sub-inventory item.
  * The `Player` class refers to it as the "fifth slot" as its permanent slot number is encoded as `0x85`.
  * The inventory of this object is accessed using a game world `Locker` object (`mb_locker`).
  */
class LockerContainer extends PlanetSideServerObject
  with Container {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private val inventory = GridInventory(30, 20)

  def Faction : PlanetSideEmpire.Value = faction

  override def Faction_=(fact : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = fact
    Faction
  }

  def Inventory : GridInventory = inventory

  def VisibleSlots : Set[Int] = Set.empty[Int]

  def Definition : EquipmentDefinition = GlobalDefinitions.locker_container
}

object LockerContainer {
  def apply() : LockerContainer = {
    new LockerContainer()
  }
}

class LockerEquipment(locker : LockerContainer) extends Equipment
  with Container {
  private val obj = locker

  override def GUID : PlanetSideGUID = obj.GUID

  override def Faction : PlanetSideEmpire.Value = obj.Faction

  def Inventory : GridInventory = obj.Inventory

  def VisibleSlots : Set[Int] = Set.empty[Int]

  def Definition : EquipmentDefinition = obj.Definition
}

class LockerContainerControl(locker : LockerContainer) extends Actor
  with Containable {
  def ContainerObject = locker

  def receive : Receive = containerBehavior
    .orElse {
      case _ => ;
    }
}
