// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The tab used to select an implant to be utilized by the player.
  * A maximum of three implants can be obtained by any player at a time depending on the player's battle rank.
  * Only implants may be returned to the interface defined by this page.
  * @see `ImplantDefinition`
  * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a `CertificationType` value
  */
final case class ImplantPage(stock: Map[String, ImplantDefinition]) extends Tab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some(implant: ImplantDefinition) =>
        Terminal.LearnImplant(implant)
      case None =>
        Terminal.NoDeal()
    }
  }

  override def Sell(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some(implant: ImplantDefinition) =>
        Terminal.SellImplant(implant)
      case None =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    sender ! msg
  }
}
