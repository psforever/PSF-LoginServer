package net.psforever.persistence

import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.serverobject.terminals.ImplantTerminalDefinition

case class Implant(
    name: String,
    avatarId: Int
) {
  def toImplantDefinition: ImplantDefinition = {
    ImplantTerminalDefinition.implants(name)
  }
}
