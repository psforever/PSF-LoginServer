// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The definition for any `Terminal` that is of a type "spawn_terminal."
  * A "spawn_terminal" is somewhat like the `matrix_terminalc` of an advanced mobile spawn unit, but inside of facilities.
  */
class SpawnTerminalDefinition extends TerminalDefinition(812) {
  Name = "spawn_terminal"

  override def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
