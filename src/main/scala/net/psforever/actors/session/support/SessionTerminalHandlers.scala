// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//
import net.psforever.actors.session.AvatarActor
import net.psforever.login.WorldSession.{BuyNewEquipmentPutInInventory, SellEquipmentFromInventory}
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.guid.{StraightforwardTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.EffectTarget
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{ProximityDefinition, ProximityUnit, Terminal}
import net.psforever.packet.game.{ItemTransactionMessage, ItemTransactionResultMessage,ProximityTerminalUseMessage, UnuseItemMessage}
import net.psforever.types.{PlanetSideGUID, TransactionType, Vector3}

class SessionTerminalHandlers(
                               val sessionData: SessionData,
                               avatarActor: typed.ActorRef[AvatarActor.Command],
                               implicit val context: ActorContext
                             ) extends CommonSessionInterfacingFunctionality {
  private[support] var lastTerminalOrderFulfillment: Boolean = true
  private[support] var usingMedicalTerminal: Option[PlanetSideGUID] = None

  /* packets */

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit = {
    val ItemTransactionMessage(terminalGuid, transactionType, _, itemName, _, _) = pkt
    continent.GUID(terminalGuid) match {
      case Some(term: Terminal) if lastTerminalOrderFulfillment =>
        log.info(s"${player.Name} is submitting an order - $transactionType of $itemName")
        lastTerminalOrderFulfillment = false
        sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
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
        HandleProximityTerminalUse(obj)
      case Some(obj) =>
        log.warn(s"ProximityTerminalUse: ${obj.Definition.Name} guid=${objectGuid.guid} is not ready to implement proximity effects")
      case None =>
        log.error(s"ProximityTerminalUse: ${player.Name} can not find an object with guid ${objectGuid.guid}")
    }
  }

  /* response handler */

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
        lastTerminalOrderFulfillment = true
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))

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
        lastTerminalOrderFulfillment = true

      case Terminal.SellCertification(cert) =>
        avatarActor ! AvatarActor.SellCertification(msg.terminal_guid, cert)
        lastTerminalOrderFulfillment = true

      case Terminal.LearnImplant(implant) =>
        avatarActor ! AvatarActor.LearnImplant(msg.terminal_guid, implant)
        lastTerminalOrderFulfillment = true

      case Terminal.SellImplant(implant) =>
        avatarActor ! AvatarActor.SellImplant(msg.terminal_guid, implant)
        lastTerminalOrderFulfillment = true

      case Terminal.BuyVehicle(vehicle, _, _)
        if tplayer.avatar.purchaseCooldown(vehicle.Definition).nonEmpty || tplayer.spectator =>
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
        lastTerminalOrderFulfillment = true

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
            TaskWorkflow.execute(registerVehicleFromSpawnPad(vehicle, pad, term))
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = true))
            if (GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
              sendResponse(UnuseItemMessage(player.GUID, msg.terminal_guid))
            }
          }.orElse {
          log.error(
            s"${tplayer.Name} wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it"
          )
          sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
          None
        }

      case Terminal.NoDeal() if msg != null =>
        val transaction = msg.transaction_type
        log.warn(s"NoDeal: ${tplayer.Name} made a request but the terminal rejected the ${transaction.toString} order")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, transaction, success = false))
        lastTerminalOrderFulfillment = true

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
        lastTerminalOrderFulfillment = true
    }
  }

  /* support */

  /**
   * Construct tasking that adds a completed and registered vehicle into the scene.
   * The major difference between `RegisterVehicle` and `RegisterVehicleFromSpawnPad` is the assumption that this vehicle lacks an internal `Actor`.
   * Before being finished, that vehicle is supplied an `Actor` such that it may function properly.
   * This function wraps around `RegisterVehicle` and is used in case, prior to this event,
   * the vehicle is being brought into existence from scratch and was never a member of any `Zone`.
   * @param vehicle the `Vehicle` object
   * @see `RegisterVehicle`
   * @return a `TaskBundle` message
   */
  private[session] def registerVehicleFromSpawnPad(vehicle: Vehicle, pad: VehicleSpawnPad, terminal: Terminal): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle  = vehicle
        private val localPad      = pad.Actor
        private val localTerminal = terminal
        private val localPlayer   = player

        override def description(): String = s"register a ${localVehicle.Definition.Name} for spawn pad"

        def action(): Future[Any] = {
          localPad ! VehicleSpawnPad.VehicleOrder(localPlayer, localVehicle, localTerminal)
          Future(true)
        }
      },
      List(sessionData.registerVehicle(vehicle))
    )
  }

  /**
   * na
   * @param terminal na
   */
  def HandleProximityTerminalUse(terminal: Terminal with ProximityUnit): Unit = {
    val term_guid      = terminal.GUID
    val targets        = FindProximityUnitTargetsInScope(terminal)
    val currentTargets = terminal.Targets
    targets.foreach { target =>
      if (!currentTargets.contains(target)) {
        StartUsingProximityUnit(terminal, target)
      } else if (targets.isEmpty) {
        log.warn(
          s"HandleProximityTerminalUse: ${player.Name} could not find valid targets to give to proximity unit ${terminal.Definition.Name}@${term_guid.guid}"
        )
      }
    }
  }

  /**
   * na
   * @param terminal na
   * @return na
   */
  def FindProximityUnitTargetsInScope(terminal: Terminal with ProximityUnit): Seq[PlanetSideGameObject] = {
    terminal.Definition.asInstanceOf[ProximityDefinition].TargetValidation.keySet.collect {
      case EffectTarget.Category.Player                                   => Some(player)
      case EffectTarget.Category.Vehicle | EffectTarget.Category.Aircraft => continent.GUID(player.VehicleSeated)
    } collect {
      case Some(a) => a
    } toSeq
  }

  /**
   * Queue a proximity-based service.
   * @param terminal the proximity-based unit
   * @param target the entity that is being considered for terminal operation
   */
  def StartUsingProximityUnit(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject): Unit = {
    val term_guid = terminal.GUID
    if (player.isAlive) {
      target match {
        case _: Player =>
          terminal.Actor ! CommonMessages.Use(player, Some(target))
        case _: Vehicle =>
          terminal.Actor ! CommonMessages.Use(player, Some(target))
        case _ =>
          log.error(
            s"StartUsingProximityUnit: ${player.Name}, this ${terminal.Definition.Name} can not deal with target $target"
          )
      }
      terminal.Definition match {
        case GlobalDefinitions.adv_med_terminal | GlobalDefinitions.medical_terminal =>
          usingMedicalTerminal = Some(term_guid)
        case _ => ()
      }
    }
  }

  /**
   * Stop using a proximity-base service.
   * If the suggested terminal detects our player or our player's vehicle as a valid target for its effect,
   * inform it that we wish it stop affecting the discovered target(s).
   * @param terminal the proximity-based unit
   */
  def StopUsingProximityUnit(terminal: Terminal with ProximityUnit): Unit = {
    FindProximityUnitTargetsInScope(terminal).foreach { target =>
      LocalStopUsingProximityUnit(terminal)
      terminal.Actor ! CommonMessages.Unuse(player, Some(target))
    }
  }

  /**
   * Stop using a proximity-base service.
   * Callback to handle flags specific to `SessionActor`.
   * Special note is warranted when determining the identity of the proximity terminal.
   * Medical terminals of both varieties can be cancelled by movement.
   * Other sorts of proximity-based units are put on a timer.
   * @param terminal the proximity-based unit
   */
  def LocalStopUsingProximityUnit(terminal: Terminal with ProximityUnit): Unit = {
    ForgetAllProximityTerminals(terminal.GUID)
  }

  /**
   * Cease all current interactions with proximity-based units.
   * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
   * This operations may invoke callback messages.
   * @see `postStop`
   */
  def CancelAllProximityUnits(): Unit = {
    usingMedicalTerminal.foreach { CancelAllProximityUnits }
  }

  /**
   * Cease all current interactions with proximity-based units.
   * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
   * This operations may invoke callback messages.
   * @param guid globally unique identifier for a proximity terminal
   * @see `postStop`
   */
  def CancelAllProximityUnits(guid: PlanetSideGUID): Unit = {
    continent.GUID(guid).collect {
      case terminal: Terminal with ProximityUnit =>
        FindProximityUnitTargetsInScope(terminal).foreach(target =>
          terminal.Actor ! CommonMessages.Unuse(player, Some(target))
        )
        ForgetAllProximityTerminals(guid)
    }
  }

  /**
   * na
   */
  def ForgetAllProximityTerminals(termGuid: PlanetSideGUID): Unit = {
    if (usingMedicalTerminal.contains(termGuid)) {
      usingMedicalTerminal = None
    }
  }
}
