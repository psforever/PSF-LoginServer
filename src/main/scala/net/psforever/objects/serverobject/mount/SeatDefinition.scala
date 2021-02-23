// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player

class SeatDefinition extends MountableDefinition[Player] {
  Name = "mount"
  var weaponMount: Option[Int] = None

  var restriction: MountRestriction[Player] = NoMax

  var bailable: Boolean = false

  def ControlledWeapon: Option[Int] = weaponMount

  def ControlledWeapon_=(wep: Int): Option[Int] = {
    ControlledWeapon_=(Some(wep))
  }

  def ControlledWeapon_=(wep: Option[Int]): Option[Int] = {
    weaponMount = wep
    ControlledWeapon
  }
}
