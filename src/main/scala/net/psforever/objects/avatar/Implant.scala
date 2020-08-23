package net.psforever.objects.avatar

import net.psforever.objects.definition.ImplantDefinition
import net.psforever.packet.game.objectcreate.ImplantEntry

case class Implant(
    definition: ImplantDefinition,
    active: Boolean = false,
    initialized: Boolean = false
    //initializationTime: FiniteDuration
) {
  def toEntry: ImplantEntry = {
    // TODO initialization time?
    new ImplantEntry(definition.implantType, None, active)
  }
}
