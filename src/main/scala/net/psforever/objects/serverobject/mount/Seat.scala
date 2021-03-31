// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player

class Seat(private val sdef: SeatDefinition) extends MountableSpace[Player] {
  override protected def testToMount(target: Player): Boolean = target.VehicleSeated.isEmpty && super.testToMount(target)

  def definition: SeatDefinition = sdef
}
