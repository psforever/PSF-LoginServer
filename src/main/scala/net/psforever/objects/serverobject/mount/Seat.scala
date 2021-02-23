// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player

class Seat(private val sdef: MountableDefinition[Player]) extends SingleMountableSpace[Player] {
  override protected def testToMount(target: Player): Boolean = target.VehicleSeated.isEmpty && super.testToMount(target)

  def ControlledWeapon: Option[Int] = sdef match {
    case s: SeatDefinition => s.weaponMount
    case _                 => None
  }

  def definition: MountableDefinition[Player] = sdef
}
