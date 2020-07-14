// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player

class GeneratorTerminalDefinition(objId: Int) extends TerminalDefinition(objId) {
  Name = "generator_terminal"
  def Request(player: Player, msg: Any): Terminal.Exchange = Terminal.NoDeal()
}
