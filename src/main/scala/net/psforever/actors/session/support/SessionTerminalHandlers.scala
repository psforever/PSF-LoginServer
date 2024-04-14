// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.guid.GUIDTask
import net.psforever.packet.game.FavoritesRequest

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

  override protected[support] def actionsToCancel(): Unit = {
    lastTerminalOrderFulfillment = true
    usingMedicalTerminal = None
  }
}
