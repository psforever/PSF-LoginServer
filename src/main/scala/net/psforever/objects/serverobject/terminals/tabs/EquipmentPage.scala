// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The tab used to produce an `Equipment` object to be used by the player.
  * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a curried function that produces an `Equipment` object
  */
final case class EquipmentPage(stock: Map[String, () => Equipment]) extends ScrutinizedTab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some(item) =>
        val createdItem = item()
        if (!Exclude.exists(_.checkRule(player, msg, createdItem))) {
          Terminal.BuyEquipment(createdItem)
        } else {
          Terminal.NoDeal()
        }
      case _ =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    sender ! msg
  }
}
