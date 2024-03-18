// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.objects.Player
import net.psforever.types.BailType

class Seat(private val sdef: SeatDefinition) extends MountableSpace[Player] {
  override protected def testToMount(target: Player): Boolean = target.VehicleSeated.isEmpty && super.testToMount(target)

  override def unmount(target: Option[Player], bailType: BailType.Value): Option[Player] = {
    val outcome = super.unmount(target, bailType)
    target.collect {
      case p if outcome.isEmpty && !isOccupiedBy(p) =>
        p.VehicleSeated = None
    }
    outcome
  }

  def definition: SeatDefinition = sdef
}
