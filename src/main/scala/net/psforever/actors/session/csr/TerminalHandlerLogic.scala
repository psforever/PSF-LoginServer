// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, SessionTerminalHandlers, TerminalHandlerFunctions}
import net.psforever.login.WorldSession.{BuyNewEquipmentPutInInventory, SellEquipmentFromInventory}
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.guid.TaskWorkflow
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.objects.sourcing.AmenitySource
import net.psforever.objects.vital.TerminalUsedActivity
import net.psforever.packet.game.{FavoritesAction, FavoritesRequest, ItemTransactionMessage, ItemTransactionResultMessage, ProximityTerminalUseMessage, UnuseItemMessage}
import net.psforever.types.{TransactionType, Vector3}

object TerminalHandlerLogic {
  def apply(ops: SessionTerminalHandlers): TerminalHandlerLogic = {
    new TerminalHandlerLogic(ops, ops.context)
  }
}

class TerminalHandlerLogic(val ops: SessionTerminalHandlers, implicit val context: ActorContext) extends TerminalHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit = {
    val ItemTransactionMessage(terminalGuid, transactionType, _, itemName, _, _) = pkt
    continent.GUID(terminalGuid) match {
      case Some(term: Terminal) if ops.lastTerminalOrderFulfillment =>
        val msg: String = if (itemName.nonEmpty) s" of $itemName" else ""
        log.info(s"${player.Name} is submitting an order - a $transactionType from a ${term.Definition.Name}$msg")
        ops.lastTerminalOrderFulfillment = false
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        term.Actor ! Terminal.Request(player, pkt)
      case Some(_: Terminal) =>
        log.warn(s"Please Wait until your previous order has been fulfilled, ${player.Name}")
      case Some(obj) =>
        log.error(s"ItemTransaction: ${obj.Definition.Name} is not a terminal, ${player.Name}")
      case _ =>
        log.error(s"ItemTransaction: entity with guid=${terminalGuid.guid} does not exist, ${player.Name}")
    }
  }

  def handleProximityTerminalUse(pkt: ProximityTerminalUseMessage): Unit = {
    val ProximityTerminalUseMessage(_, objectGuid, _) = pkt
    continent.GUID(objectGuid) match {
      case Some(obj: Terminal with ProximityUnit) =>
        ops.HandleProximityTerminalUse(obj)
      case Some(obj) =>
        log.warn(s"ProximityTerminalUse: ${obj.Definition.Name} guid=${objectGuid.guid} is not ready to implement proximity effects")
      case None =>
        log.error(s"ProximityTerminalUse: ${player.Name} can not find an object with guid ${objectGuid.guid}")
    }
  }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = {
    val FavoritesRequest(_, loadoutType, action, line, label) = pkt
    sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
    action match {
      case FavoritesAction.Save   =>
        avatarActor ! AvatarActor.SaveLoadout(player, loadoutType, label, line)
      case FavoritesAction.Delete =>
        avatarActor ! AvatarActor.DeleteLoadout(player, loadoutType, line)
      case FavoritesAction.Unknown =>
        log.warn(s"FavoritesRequest: ${player.Name} requested an unknown favorites action")
    }
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
        continent.map.terminalToSpawnPad
          .find { case (termid, _) => termid == msg.terminal_guid.guid }
          .map { case (a: Int, b: Int) => (continent.GUID(a), continent.GUID(b)) }
          .collect { case (Some(term: Terminal), Some(pad: VehicleSpawnPad)) =>
            avatarActor ! AvatarActor.UpdatePurchaseTime(vehicle.Definition)
            vehicle.Faction = tplayer.Faction
            vehicle.Position = pad.Position
            vehicle.Orientation = pad.Orientation + Vector3.z(pad.Definition.VehicleCreationZOrientOffset)
            //default loadout, weapons
            val vWeapons = vehicle.Weapons
            weapons.foreach { entry =>
              vWeapons.get(entry.start) match {
                case Some(slot) =>
                  entry.obj.Faction = tplayer.Faction
                  slot.Equipment = None
                  slot.Equipment = entry.obj
                case None =>
                  log.warn(
                    s"BuyVehicle: ${player.Name} tries to apply default loadout to $vehicle on spawn, but can not find a mounted weapon for ${entry.start}"
                  )
              }
            }
            //default loadout, trunk
            val vTrunk = vehicle.Trunk
            vTrunk.Clear()
            trunk.foreach { entry =>
              entry.obj.Faction = tplayer.Faction
              vTrunk.InsertQuickly(entry.start, entry.obj)
            }
            TaskWorkflow.execute(ops.registerVehicleFromSpawnPad(vehicle, pad, term))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = true))
            if (GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
              sendResponse(UnuseItemMessage(player.GUID, msg.terminal_guid))
            }
            player.LogActivity(TerminalUsedActivity(AmenitySource(term), msg.transaction_type))
          }
          .orElse {
            log.error(
              s"${tplayer.Name} wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it"
            )
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
            None
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
