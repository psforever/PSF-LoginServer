// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.EquipmentSlot

trait MountedWeapons {
  this: PlanetSideGameObject =>
  protected var weapons: Map[Int, EquipmentSlot] = Map[Int, EquipmentSlot]()

  def Weapons: Map[Int, EquipmentSlot] = weapons

  def Definition: MountedWeaponsDefinition
}
