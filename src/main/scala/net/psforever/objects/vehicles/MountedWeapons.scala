// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.mount.{Mountable, Seat => Chair}

trait MountedWeapons {
  this: PlanetSideGameObject with Mountable with Container =>

  def Weapons: Map[Int, EquipmentSlot]

  /**
    * Given a valid mount number, retrieve an index where the weapon controlled from this mount is mounted.
    * @param seatNumber the mount number
    * @return a mounted weapon by index, or `None` if either the mount doesn't exist or there is no controlled weapon
    */
  def WeaponControlledFromSeat(seatNumber: Int): Option[Equipment] = {
    Seat(seatNumber) match {
      case Some(seat) =>
        wepFromSeat(seat)
      case None =>
        None
    }
  }

  private def wepFromSeat(seat: Chair): Option[Equipment] = {
    seat.ControlledWeapon match {
      case Some(index) =>
        ControlledWeapon(index)
      case None =>
        None
    }
  }

  def ControlledWeapon(wepNumber: Int): Option[Equipment]
}
