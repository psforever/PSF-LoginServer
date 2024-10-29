// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, SessionTerminalHandlers, TerminalHandlerFunctions}
import net.psforever.login.WorldSession.{BuyNewEquipmentPutInInventory, SellEquipmentFromInventory}
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.guid.TaskWorkflow
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.{FavoritesRequest, ItemTransactionMessage, ItemTransactionResultMessage, ProximityTerminalUseMessage}
import net.psforever.types.TransactionType

object TerminalHandlerLogic {
  def apply(ops: SessionTerminalHandlers): TerminalHandlerLogic = {
    new TerminalHandlerLogic(ops, ops.context)
  }
}

class TerminalHandlerLogic(val ops: SessionTerminalHandlers, implicit val context: ActorContext) extends TerminalHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    ops.handleItemTransaction(pkt)
  }

  def handleProximityTerminalUse(pkt: ProximityTerminalUseMessage): Unit = {
    ops.handleProximityTerminalUse(pkt)
  }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = {
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    ops.handleFavoritesRequest(pkt)
  }

  /**
   * na
   * @param tplayer na
   * @param msg     na
   * @param order   na
   */
  def handle(tplayer: Player, msg: ItemTransactionMessage, order: Terminal.Exchange): Unit = {
    order match {
      case Terminal.BuyEquipment(item)
        if tplayer.avatar.purchaseCooldown(item.Definition).nonEmpty =>
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
        ops.lastTerminalOrderFulfillment = true

      case Terminal.BuyEquipment(item) =>
        avatarActor ! AvatarActor.UpdatePurchaseTime(item.Definition)
        TaskWorkflow.execute(BuyNewEquipmentPutInInventory(
          continent.GUID(tplayer.VehicleSeated) match {
            case Some(v: Vehicle) => v
            case _ => player
          },
          tplayer,
          msg.terminal_guid
        )(item))

      case Terminal.SellEquipment() =>
        SellEquipmentFromInventory(tplayer, tplayer, msg.terminal_guid)(Player.FreeHandSlot)

      case Terminal.LearnCertification(cert) =>
        avatarActor ! AvatarActor.LearnCertification(msg.terminal_guid, cert)
        ops.lastTerminalOrderFulfillment = true

      case Terminal.SellCertification(cert) =>
        avatarActor ! AvatarActor.SellCertification(msg.terminal_guid, cert)
        ops.lastTerminalOrderFulfillment = true

      case Terminal.LearnImplant(implant) =>
        avatarActor ! AvatarActor.LearnImplant(msg.terminal_guid, implant)
        ops.lastTerminalOrderFulfillment = true

      case Terminal.SellImplant(implant) =>
        avatarActor ! AvatarActor.SellImplant(msg.terminal_guid, implant)
        ops.lastTerminalOrderFulfillment = true

      case Terminal.BuyVehicle(vehicle, _, _)
        if tplayer.avatar.purchaseCooldown(vehicle.Definition).nonEmpty =>
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
        ops.lastTerminalOrderFulfillment = true

      case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
        ops.buyVehicle(msg.terminal_guid, msg.transaction_type, vehicle, weapons, trunk)
          .collect {
            case _: Vehicle =>
              avatarActor ! AvatarActor.UpdatePurchaseTime(vehicle.Definition)
          }
        ops.lastTerminalOrderFulfillment = true

      case Terminal.NoDeal() if msg != null =>
        val transaction = msg.transaction_type
        log.warn(s"NoDeal: ${tplayer.Name} made a request but the terminal rejected the ${transaction.toString} order")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, transaction, success = false))
        ops.lastTerminalOrderFulfillment = true

      case _ =>
        val terminal = msg.terminal_guid.guid
        continent.GUID(terminal) match {
          case Some(term: Terminal) =>
            log.warn(s"NoDeal?: ${tplayer.Name} made a request but the ${term.Definition.Name}#$terminal rejected the missing order")
          case Some(_) =>
            log.warn(s"NoDeal?: ${tplayer.Name} made a request to a non-terminal entity#$terminal")
          case None =>
            log.warn(s"NoDeal?: ${tplayer.Name} made a request to a missing entity#$terminal")
        }
        ops.lastTerminalOrderFulfillment = true
    }
  }
}
