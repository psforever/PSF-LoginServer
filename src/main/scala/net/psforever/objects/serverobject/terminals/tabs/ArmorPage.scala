// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

/**
  * The tab used to select an exo-suit to be worn by the player.
  * @see `ExoSuitType`
  * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a tuple composed of an `ExoSuitType` value and a subtype value
  */
final case class ArmorPage(stock: Map[String, (ExoSuitType.Value, Int)]) extends Tab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some((suit: ExoSuitType.Value, subtype: Int)) =>
        Terminal.BuyExosuit(suit, subtype)
      case _ =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.player.Actor ! msg
  }
}
