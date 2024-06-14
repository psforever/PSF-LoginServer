package net.psforever.persistence

import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalDefinition

case class Implant(
    name: String,
    avatarId: Int,
    timer: Int = 0 //seconds to initialize
) {
  def toImplantDefinition: ImplantDefinition = {
    ImplantTerminalDefinition.implants(name)
  }
}
