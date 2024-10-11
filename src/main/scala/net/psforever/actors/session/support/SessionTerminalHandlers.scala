// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.sourcing.AmenitySource
import net.psforever.objects.vital.TerminalUsedActivity
import net.psforever.packet.game.{FavoritesAction, FavoritesRequest, ItemTransactionResultMessage, UnuseItemMessage}
import net.psforever.types.{TransactionType, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.guid.{StraightforwardTask, TaskBundle}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.EffectTarget
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.{ProximityDefinition, ProximityUnit, Terminal}
import net.psforever.packet.game.{ItemTransactionMessage,ProximityTerminalUseMessage}
import net.psforever.types.PlanetSideGUID

trait TerminalHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionTerminalHandlers

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit

  def handleProximityTerminalUse(pkt: ProximityTerminalUseMessage): Unit

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit

  def handle(tplayer: Player, msg: ItemTransactionMessage, order: Terminal.Exchange): Unit
}

class SessionTerminalHandlers(
                               val sessionLogic: SessionData,
                               val avatarActor: typed.ActorRef[AvatarActor.Command],
                               implicit val context: ActorContext
                             ) extends CommonSessionInterfacingFunctionality {
  private[session] var lastTerminalOrderFulfillment: Boolean = true
  private[session] var usingMedicalTerminal: Option[PlanetSideGUID] = None

  def handleItemTransaction(pkt: ItemTransactionMessage): Unit = {
    val ItemTransactionMessage(terminalGuid, transactionType, _, itemName, _, _) = pkt
    continent.GUID(terminalGuid) match {
      case Some(term: Terminal) if lastTerminalOrderFulfillment =>
        val msg: String = if (itemName.nonEmpty) s" of $itemName" else ""
        log.info(s"${player.Name} is submitting an order - a $transactionType from a ${term.Definition.Name}$msg")
        lastTerminalOrderFulfillment = false
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
        performProximityTerminalUse(obj)
      case Some(obj) =>
        log.warn(s"ProximityTerminalUse: ${obj.Definition.Name} guid=${objectGuid.guid} is not ready to implement proximity effects")
      case None =>
        log.error(s"ProximityTerminalUse: ${player.Name} can not find an object with guid ${objectGuid.guid}")
    }
  }

  def handleFavoritesRequest(pkt: FavoritesRequest): Unit = {
    val FavoritesRequest(_, loadoutType, action, line, label) = pkt
    action match {
      case FavoritesAction.Save   =>
        avatarActor ! AvatarActor.SaveLoadout(player, loadoutType, label, line)
      case FavoritesAction.Delete =>
        avatarActor ! AvatarActor.DeleteLoadout(player, loadoutType, line)
      case FavoritesAction.Unknown =>
        log.warn(s"FavoritesRequest: ${player.Name} requested an unknown favorites action")
    }
  }

   def buyVehicle(
                   terminalGuid: PlanetSideGUID,
                   transactionType: TransactionType.Value,
                   vehicle: Vehicle,
                   weapons: List[InventoryItem],
                   trunk: List[InventoryItem]
                 ): Unit = {
     continent.map.terminalToSpawnPad
       .find { case (termid, _) => termid == terminalGuid.guid }
       .map { case (a: Int, b: Int) => (continent.GUID(a), continent.GUID(b)) }
       .collect { case (Some(term: Terminal), Some(pad: VehicleSpawnPad)) =>
         avatarActor ! AvatarActor.UpdatePurchaseTime(vehicle.Definition)
         vehicle.Faction = player.Faction
         vehicle.Position = pad.Position
         vehicle.Orientation = pad.Orientation + Vector3.z(pad.Definition.VehicleCreationZOrientOffset)
         //default loadout, weapons
         val vWeapons = vehicle.Weapons
         weapons.foreach { entry =>
           vWeapons.get(entry.start) match {
             case Some(slot) =>
               entry.obj.Faction = player.Faction
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
           entry.obj.Faction = player.Faction
           vTrunk.InsertQuickly(entry.start, entry.obj)
         }
         TaskWorkflow.execute(registerVehicleFromSpawnPad(vehicle, pad, term))
         sendResponse(ItemTransactionResultMessage(terminalGuid, TransactionType.Buy, success = true))
         if (GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
           sendResponse(UnuseItemMessage(player.GUID, terminalGuid))
         }
         player.LogActivity(TerminalUsedActivity(AmenitySource(term), transactionType))
       }
       .orElse {
         log.error(
           s"${player.Name} wanted to spawn a vehicle, but there was no spawn pad associated with terminal ${terminalGuid.guid} to accept it"
         )
         sendResponse(ItemTransactionResultMessage(terminalGuid, TransactionType.Buy, success = false))
         None
       }
   }

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
  def registerVehicleFromSpawnPad(vehicle: Vehicle, pad: VehicleSpawnPad, terminal: Terminal): TaskBundle = {
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
      List(registerVehicle(vehicle))
    )
  }

  /**
   * Construct tasking that adds a completed and registered vehicle into the scene.
   * Use this function to renew the globally unique identifiers on a vehicle that has already been added to the scene once.
   * @param vehicle the `Vehicle` object
   * @see `RegisterVehicleFromSpawnPad`
   * @return a `TaskBundle` message
   */
  def registerVehicle(vehicle: Vehicle): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle = vehicle

        override def description(): String = s"register a ${localVehicle.Definition.Name}"

        def action(): Future[Any] = Future(true)
      },
      List(GUIDTask.registerVehicle(continent.GUID, vehicle))
    )
  }

  /**
   * na
   * @param terminal na
   */
  def performProximityTerminalUse(terminal: Terminal with ProximityUnit): Unit = {
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
   * Pair with `actionsToCancel`, except when logging out (stopping).
   * This operations may invoke callback messages.
   * @see `postStop`
   */
  def CancelAllProximityUnits(): Unit = {
    usingMedicalTerminal.foreach { CancelAllProximityUnits }
  }

  /**
   * Cease all current interactions with proximity-based units.
   * Pair with `actionsToCancel`, except when logging out (stopping).
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

  override protected[session] def actionsToCancel(): Unit = {
    lastTerminalOrderFulfillment = true
    usingMedicalTerminal = None
  }
}
