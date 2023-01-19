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
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ItemTransactionMessage, ItemTransactionResultMessage,ProximityTerminalUseMessage, UnuseItemMessage}
import net.psforever.types.{PlanetSideGUID, TransactionType, Vector3}

class SessionTerminalHandlers(
                               val sessionData: SessionData,
                               avatarActor: typed.ActorRef[AvatarActor.Command],
                               implicit val context: ActorContext
                             ) extends CommonSessionInterfacingFunctionality {
  private[support] var lastTerminalOrderFulfillment: Boolean = true
  private[support] var usingMedicalTerminal: Option[PlanetSideGUID] = None

  /* */

  def handleItemTransaction(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case msg: ItemTransactionMessage =>
        continent.GUID(msg.terminal_guid) match {
          case Some(term: Terminal) =>
            if (lastTerminalOrderFulfillment) {
              log.trace(s"ItemTransactionMessage: ${player.Name} is submitting an order")
              lastTerminalOrderFulfillment = false
              sessionData.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
              term.Actor ! Terminal.Request(player, msg)
            }
          case Some(obj: PlanetSideGameObject) =>
            log.error(s"ItemTransaction: $obj is not a terminal, ${player.Name}")
          case _ =>
            log.error(s"ItemTransaction: ${msg.terminal_guid} does not exist, ${player.Name}")
        }
      case _ => ;
    }
  }

  def handleProximityTerminalUse(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case ProximityTerminalUseMessage(_, object_guid, _) =>
        continent.GUID(object_guid) match {
          case Some(obj: Terminal with ProximityUnit) =>
            HandleProximityTerminalUse(obj)
          case Some(obj) =>
            log.warn(s"ProximityTerminalUse: $obj does not have proximity effects for ${player.Name}")
          case None =>
            log.error(s"ProximityTerminalUse: ${player.Name} can not find an object with guid $object_guid")
        }
      case _ => ;
    }
  }

  /**
   * na
   *
   * @param tplayer na
   * @param msg     na
   * @param order   na
   */
  def handle(tplayer: Player, msg: ItemTransactionMessage, order: Terminal.Exchange): Unit = {
    order match {
      case Terminal.BuyEquipment(item) =>
        tplayer.avatar.purchaseCooldown(item.Definition) match {
          case Some(_) =>
            lastTerminalOrderFulfillment = true
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
          case None =>
            avatarActor ! AvatarActor.UpdatePurchaseTime(item.Definition)
            TaskWorkflow.execute(BuyNewEquipmentPutInInventory(
              continent.GUID(tplayer.VehicleSeated) match { case Some(v: Vehicle) => v; case _ => player },
              tplayer,
              msg.terminal_guid
            )(item))
        }

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

      case Terminal.BuyVehicle(vehicle, weapons, trunk) =>
        tplayer.avatar.purchaseCooldown(vehicle.Definition) match {
          case Some(_) =>
            sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
          case None =>
            continent.map.terminalToSpawnPad
              .find { case (termid, _) => termid == msg.terminal_guid.guid }
              .collect {
                case (a: Int, b: Int) => (continent.GUID(a), continent.GUID(b))
                case _ => (None, None)
              }
              .get match {
              case (Some(term: Terminal), Some(pad: VehicleSpawnPad)) =>
                vehicle.Faction = tplayer.Faction
                vehicle.Position = pad.Position
                vehicle.Orientation = pad.Orientation + Vector3.z(pad.Definition.VehicleCreationZOrientOffset)
                //default loadout, weapons
                val vWeapons = vehicle.Weapons
                weapons.foreach(entry => {
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
                })
                //default loadout, trunk
                val vTrunk = vehicle.Trunk
                vTrunk.Clear()
                trunk.foreach(entry => {
                  entry.obj.Faction = tplayer.Faction
                  vTrunk.InsertQuickly(entry.start, entry.obj)
                })
                TaskWorkflow.execute(registerVehicleFromSpawnPad(vehicle, pad, term))
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = true))
                if (GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
                  sendResponse(UnuseItemMessage(player.GUID, msg.terminal_guid))
                }
              case _ =>
                log.error(
                  s"${tplayer.Name} wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${msg.terminal_guid} to accept it"
                )
                sendResponse(ItemTransactionResultMessage(msg.terminal_guid, TransactionType.Buy, success = false))
            }
        }
        lastTerminalOrderFulfillment = true

      case Terminal.NoDeal() =>
        val order: String = if (msg == null) {
          "missing order"
        } else {
          s"${msg.transaction_type} order"
        }
        log.warn(s"NoDeal: ${tplayer.Name} made a request but the terminal rejected the $order")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, msg.transaction_type, success = false))
        lastTerminalOrderFulfillment = true

      case _ =>
        val transaction = msg.transaction_type
        log.warn(s"n/a: ${tplayer.Name} made a $transaction request but terminal#${msg.terminal_guid.guid} is missing or wrong")
        sendResponse(ItemTransactionResultMessage(msg.terminal_guid, transaction, success = false))
        lastTerminalOrderFulfillment = true
    }
  }

  /* */

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
    targets.foreach(target => {
      if (!currentTargets.contains(target)) {
        StartUsingProximityUnit(terminal, target)
      } else if (targets.isEmpty) {
        log.warn(
          s"HandleProximityTerminalUse: ${player.Name} could not find valid targets to give to proximity unit ${terminal.Definition.Name}@${term_guid.guid}"
        )
      }
    })
  }

  /**
   * na
   * @param terminal na
   * @return na
   */
  def FindProximityUnitTargetsInScope(terminal: Terminal with ProximityUnit): Seq[PlanetSideGameObject] = {
    terminal.Definition.asInstanceOf[ProximityDefinition].TargetValidation.keySet collect {
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
    //log.trace(s"StartUsingProximityUnit: ${player.Name} wants to use ${terminal.Definition.Name}@${term_guid.guid} on $target")
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
        case _ => ;
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
      LocalStopUsingProximityUnit(terminal, target)
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
  def LocalStopUsingProximityUnit(terminal: Terminal with ProximityUnit, target: PlanetSideGameObject): Unit = {
    val term_guid = terminal.GUID
    if (usingMedicalTerminal.contains(term_guid)) {
      usingMedicalTerminal = None
    }
  }

  /**
   * Cease all current interactions with proximity-based units.
   * Pair with `PlayerActionsToCancel`, except when logging out (stopping).
   * This operations may invoke callback messages.
   * @see `postStop`
   */
  def CancelAllProximityUnits(): Unit = {
    continent.GUID(usingMedicalTerminal) match {
      case Some(terminal: Terminal with ProximityUnit) =>
        FindProximityUnitTargetsInScope(terminal).foreach(target =>
          terminal.Actor ! CommonMessages.Unuse(player, Some(target))
        )
        ForgetAllProximityTerminals(usingMedicalTerminal.get)
      case _ => ;
    }
  }

  /**
   * na
   */
  def ForgetAllProximityTerminals(term_guid: PlanetSideGUID): Unit = {
    if (usingMedicalTerminal.contains(term_guid)) {
      usingMedicalTerminal = None
    }
  }
}
