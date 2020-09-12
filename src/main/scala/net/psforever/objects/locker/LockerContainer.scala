// Copyright (c) 2017 PSForever
package net.psforever.objects.locker

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.PlanetSideEmpire

/**
  * The companion of a `Locker` that is carried with a player
  * masquerading as their sixth `EquipmentSlot` object and a sub-inventory item.
  * The inventory of this object is accessed indirectly using a game world `Locker` object (`mb_locker`) as a proxy.
  * The `Player` class refers to it as the "fifth slot".
  */
class LockerContainer(inventory: GridInventory)
  extends PlanetSideServerObject
  with Container {
  private var faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private val inv: GridInventory = inventory

  def Faction: PlanetSideEmpire.Value = faction

  override def Faction_=(fact: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    faction = fact
    Faction
  }

  def Inventory: GridInventory = inv

  def VisibleSlots: Set[Int] = Set.empty[Int]

  def Definition: EquipmentDefinition = GlobalDefinitions.locker_container
}

object LockerContainer {
  /**
    * Overloaded constructor for the standard Infantry locker container of size 30x20.
    * @return a `LockerContainer` object
    */
  def apply(): LockerContainer = {
    new LockerContainer(GridInventory(30, 20))
  }
}
