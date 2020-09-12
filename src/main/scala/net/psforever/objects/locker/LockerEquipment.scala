// Copyright (c) 2020 PSForever
package net.psforever.objects.locker

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

/**
  * A wrapper class that allows a player-facing locker component
  * to be treated standard `Equipment` object in the player's fifth (sixth) slot.
  * (The opposite is not true - the equipment does not get treated as a locker component.)
  * During packet conversion and registration and general access in terms of holsters or "equipment slots",
  * the component may be be treated the same as other existing objects at the same level.
  * The entity's ability to  be utilized like an inventory-stowable entity is not compromised.
  * @see `EquipmentSlot`
  * @see `IdentifiableEntity`
  * @see `LockerContainer`
  * @param locker the player-facing locker
  */
class LockerEquipment(locker: LockerContainer)
  extends Equipment
  with Container {
  private val obj = locker

  override def GUID: PlanetSideGUID = obj.GUID

  override def GUID_=(guid: PlanetSideGUID): PlanetSideGUID = obj.GUID_=(guid)

  override def HasGUID: Boolean = obj.HasGUID

  override def Invalidate(): Unit = obj.Invalidate()

  override def Faction: PlanetSideEmpire.Value = obj.Faction

  def Inventory: GridInventory = obj.Inventory

  def VisibleSlots: Set[Int] = Set.empty[Int]

  def Definition: EquipmentDefinition = obj.Definition
}
