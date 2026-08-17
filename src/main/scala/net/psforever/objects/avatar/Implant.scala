package net.psforever.objects.avatar

import net.psforever.objects.definition.ImplantDefinition
import net.psforever.packet.game.objectcreate.ImplantEntry

case class Implant(
    definition: ImplantDefinition,
    active: Boolean = false,
    initialized: Boolean = false,
    timer: Long = 0L
) {
  def toEntry: ImplantEntry = {
    val initState = if (!initialized) {
      if (timer > 0) {
        Some(math.max(0, ((timer - System.currentTimeMillis()) / 1000L).toInt))
      } else {
        Some(definition.InitializationDuration.toInt)
      }
    } else {
      None
    }
    new ImplantEntry(definition.implantType, initState, active)
  }
}
