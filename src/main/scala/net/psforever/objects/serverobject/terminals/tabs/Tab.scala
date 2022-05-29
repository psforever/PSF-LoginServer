// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

/**
  * A basic tab outlining the specific type of stock available from this part of the terminal's interface.
  * @see `ItemTransactionMessage`
  */
trait Tab {
  def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange
  def Sell(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = Terminal.NoDeal()
  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit
}
