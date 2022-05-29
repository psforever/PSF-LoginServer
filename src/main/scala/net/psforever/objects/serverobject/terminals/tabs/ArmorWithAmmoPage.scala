// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

/**
  * An expanded form of the tab used to select an exo-suit to be worn by the player that also provides some equipment.
  * @see `ExoSuitType`
  * @see `Equipment`
  * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a tuple composed of an `ExoSuitType` value and a subtype value
  * @param items the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a curried function that produces an `Equipment` object
  */
final case class ArmorWithAmmoPage(stock: Map[String, (ExoSuitType.Value, Int)], items: Map[String, () => Equipment])
  extends Tab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.get(msg.item_name) match {
      case Some((suit: ExoSuitType.Value, subtype: Int)) =>
        Terminal.BuyExosuit(suit, subtype)
      case _ =>
        items.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case _ =>
            Terminal.NoDeal()
        }
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    msg.response match {
      case _: Terminal.BuyExosuit => msg.player.Actor ! msg
      case _                      => sender ! msg
    }
  }
}
