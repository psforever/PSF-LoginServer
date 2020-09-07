// Copyright (c) 2020 PSForever
package net.psforever.objects.locker

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

class LockerEquipment(locker: LockerContainer) extends Equipment with Container {
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
