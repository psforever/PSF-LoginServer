// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.mount.Mountable

trait MountableWeapons
  extends MountedWeapons
  with Mountable {
  this: PlanetSideGameObject =>

  /**
    * Given a valid mount number, retrieve an index where the weapon controlled from this mount is mounted.
    * @param seatNumber the mount number
    * @return a mounted weapon by index, or `None` if either the mount doesn't exist or there is no controlled weapon
    */
  def WeaponControlledFromSeat(seatNumber: Int): Set[Equipment] = {
    Definition
      .asInstanceOf[MountableWeaponsDefinition]
      .controlledWeapons().get(seatNumber) match {
      case Some(wepNumbers) if seats.get(seatNumber).nonEmpty => wepNumbers.flatMap { controlledWeapon }
      case _                                                  => Set.empty
    }
  }

  def controlledWeapon(wepNumber: Int): Set[Equipment] = ControlledWeapon(wepNumber)

  def ControlledWeapon(wepNumber: Int): Set[Equipment] = {
    weapons.get(wepNumber) match {
      case Some(slot) =>
        slot.Equipment match {
          case Some(weapon) => Set(weapon)
          case None         => Set.empty
        }
      case _ =>
        Set.empty
    }
  }

  def Definition: MountableWeaponsDefinition
}

