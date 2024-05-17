// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, SessionTerminalHandlers, TerminalHandlerFunctions}
import net.psforever.login.WorldSession.SellEquipmentFromInventory
import net.psforever.objects.Player
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.{FavoritesRequest, ItemTransactionMessage, ItemTransactionResultMessage, ProximityTerminalUseMessage}

object TerminalHandlerLogic {
  def apply(ops: SessionTerminalHandlers): TerminalHandlerLogic = {
    new TerminalHandlerLogic(ops, ops.context)
  }
}

class TerminalHandlerLogic(val ops: SessionTerminalHandlers, implicit val context: ActorContext) extends TerminalHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit = { /* intentionally blank */ }

  def handleProximityTerminalUse(pkt: ProximityTerminalUseMessage): Unit = { /* intentionally blank */ }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = { /* intentionally blank */ }

  /**
   * na
   * @param tplayer na
   * @param msg     na
   * @param order   na
   */
  def handle(tplayer: Player, msg: ItemTransactionMessage, order: Terminal.Exchange): Unit = {
    order match {
      case Terminal.SellEquipment() =>
        SellEquipmentFromInventory(tplayer, tplayer, msg.terminal_guid)(Player.FreeHandSlot)

      case _ if msg != null =>
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, success = false))
        ops.lastTerminalOrderFulfillment = true

      case _ =>
        ops.lastTerminalOrderFulfillment = true
    }
  }
}
