// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{MountHandlerFunctions, SessionData, SessionMountHandlers}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Vehicle, Vehicles}
import net.psforever.objects.definition.{BasicDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.environment.interaction.ResetAllEnvironmentInteractions
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.serverobject.turret.{FacilityTurret, WeaponTurret}
import net.psforever.objects.vehicles.{AccessPermissionGroup, CargoBehavior}
import net.psforever.objects.vital.InGameHistory
import net.psforever.packet.game.{ChatMsg, DelayedPathMountMsg, DismountVehicleCargoMsg, DismountVehicleMsg, GenericObjectActionMessage, MountVehicleCargoMsg, MountVehicleMsg, ObjectAttachMessage, ObjectDetachMessage, PlanetsideAttributeMessage, PlayerStasisMessage, PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, ChatMessageType, PlanetSideGUID, Vector3}

import scala.concurrent.duration._

object MountHandlerLogic {
  def apply(ops: SessionMountHandlers): MountHandlerLogic = {
    new MountHandlerLogic(ops, ops.context)
  }
}

class MountHandlerLogic(val ops: SessionMountHandlers, implicit val context: ActorContext) extends MountHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleMountVehicle(pkt: MountVehicleMsg): Unit = {
    val MountVehicleMsg(_, mountable_guid, entry_point) = pkt
    sessionLogic.validObject(mountable_guid, decorator = "MountVehicle").collect {
      case obj: Mountable =>
        obj.Actor ! Mountable.TryMount(player, entry_point)
      case _ =>
        log.error(s"MountVehicleMsg: object ${mountable_guid.guid} not a mountable thing, ${player.Name}")
    }
  }

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit = {
    val DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) = pkt
    val dError: (String, Player)=> Unit = dismountError(bailType, wasKickedByDriver)
    //TODO optimize this later
    //common warning for this section
    if (player.GUID == player_guid) {
      //normally disembarking from a mount
      (sessionLogic.zoning.interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
        case out @ Some(obj: Vehicle) =>
          continent.GUID(obj.MountedIn) match {
            case Some(_: Vehicle) => None //cargo vehicle
            case _                => out  //arrangement "may" be permissible
          }
        case out @ Some(_: Mountable) =>
          out
        case _ =>
          dError(s"DismountVehicleMsg: player ${player.Name} not considered seated in a mountable entity", player)
          None
      }) match {
        case Some(obj: Mountable) =>
          obj.PassengerInSeat(player) match {
            case Some(seat_num) =>
              obj.Actor ! Mountable.TryDismount(player, seat_num, bailType)
              //short-circuit the temporary channel for transferring between zones, the player is no longer doing that
              sessionLogic.zoning.interstellarFerry = None
              // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
              //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
              //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
              //todo: kick cargo passengers out. To be added after PR #216 is merged
              obj match {
                case v: Vehicle
                  if bailType == BailType.Bailed &&
                    v.SeatPermissionGroup(seat_num).contains(AccessPermissionGroup.Driver) &&
                    v.isFlying =>
                  v.Actor ! Vehicle.Deconstruct(None) //immediate deconstruction
                case _ => ()
              }

            case None =>
              dError(s"DismountVehicleMsg: can not find where player ${player.Name}_guid is seated in mountable ${player.VehicleSeated}", player)
          }
        case _ =>
          dError(s"DismountVehicleMsg: can not find mountable entity ${player.VehicleSeated}", player)
      }
    } else {
      //kicking someone else out of a mount; need to own that mount/mountable
      val dWarn: (String, Player)=> Unit = dismountWarning(bailType, wasKickedByDriver)
      player.avatar.vehicle match {
        case Some(obj_guid) =>
          (
            (
              sessionLogic.validObject(obj_guid, decorator = "DismountVehicle/Vehicle"),
              sessionLogic.validObject(player_guid, decorator = "DismountVehicle/Player")
            ) match {
              case (vehicle @ Some(obj: Vehicle), tplayer) =>
                if (obj.MountedIn.isEmpty) (vehicle, tplayer) else (None, None)
              case (mount @ Some(_: Mountable), tplayer) =>
                (mount, tplayer)
              case _ =>
                (None, None)
            }) match {
            case (Some(obj: Mountable), Some(tplayer: Player)) =>
              obj.PassengerInSeat(tplayer) match {
                case Some(seat_num) =>
                  obj.Actor ! Mountable.TryDismount(tplayer, seat_num, bailType)
                case None =>
                  dError(s"DismountVehicleMsg: can not find where other player ${tplayer.Name} is seated in mountable $obj_guid", tplayer)
              }
            case (None, _) =>
              dWarn(s"DismountVehicleMsg: ${player.Name} can not find his vehicle", player)
            case (_, None) =>
              dWarn(s"DismountVehicleMsg: player $player_guid could not be found to kick, ${player.Name}", player)
            case _ =>
              dWarn(s"DismountVehicleMsg: object is either not a Mountable or not a Player", player)
          }
        case None =>
          dWarn(s"DismountVehicleMsg: ${player.Name} does not own a vehicle", player)
      }
    }
  }

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = {
    val MountVehicleCargoMsg(_, cargo_guid, carrier_guid, _) = pkt
    (continent.GUID(cargo_guid), continent.GUID(carrier_guid)) match {
      case (Some(cargo: Vehicle), Some(carrier: Vehicle)) =>
        carrier.CargoHolds.find({ case (_, hold) => !hold.isOccupied }) match {
          case Some((mountPoint, _)) =>
            cargo.Actor ! CargoBehavior.StartCargoMounting(carrier_guid, mountPoint)
          case _ =>
            log.warn(
              s"MountVehicleCargoMsg: ${player.Name} trying to load cargo into a ${carrier.Definition.Name} which oes not have a cargo hold"
            )
        }
      case (None, _) | (Some(_), None) =>
        log.warn(
          s"MountVehicleCargoMsg: ${player.Name} lost a vehicle while working with cargo - either $carrier_guid or $cargo_guid"
        )
      case _ => ()
    }
  }

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit = {
    val DismountVehicleCargoMsg(_, cargo_guid, bailed, _, kicked) = pkt
    continent.GUID(cargo_guid) match {
      case Some(cargo: Vehicle) =>
        cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed || kicked)
      case _ => ()
    }
  }

  /* response handlers */

  /**
   * na
   *
   * @param tplayer na
   * @param reply   na
   */
  def handle(tplayer: Player, reply: Mountable.Exchange): Unit = {
    reply match {
      case Mountable.CanMount(obj: ImplantTerminalMech, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_use")
        log.info(s"${player.Name} mounts an implant terminal")
        sessionLogic.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the orbital shuttle")
        sessionLogic.terminals.CancelAllProximityUnits()
        MountingAction(tplayer, obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistence

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.ant =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=45, obj.NtuCapacitorScaled))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition == GlobalDefinitions.quadstealth =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        //exclusive to the wraith, cloak state matches the cloak state of the driver
        //phantasm doesn't uncloak if the driver is uncloaked and no other vehicle cloaks
        obj.Cloaked = tplayer.Cloaked
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 && obj.Definition.MaxCapacitor > 0 =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if seatNumber == 0 =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the driver seat of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(GenericObjectActionMessage(obj_guid, code=11))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _)
        if obj.Definition.MaxCapacitor > 0 =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts ${
          obj.SeatPermissionGroup(seatNumber) match {
            case Some(seatType) => s"a $seatType seat (#$seatNumber)"
            case None => "a seat"
          }
        } of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=113, obj.Capacitor))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistence
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Vehicle, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${
          obj.SeatPermissionGroup(seatNumber) match {
            case Some(seatType) => s"a $seatType seat (#$seatNumber)"
            case None => "a seat"
          }
        } of the ${obj.Definition.Name}")
        val obj_guid: PlanetSideGUID = obj.GUID
        sessionLogic.terminals.CancelAllProximityUnits()
        sendResponse(PlanetsideAttributeMessage(obj_guid, attribute_type=0, obj.Health))
        sendResponse(PlanetsideAttributeMessage(obj_guid, obj.Definition.shieldUiAttribute, obj.Shields))
        sessionLogic.general.accessContainer(obj)
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        sessionLogic.keepAliveFunc = sessionLogic.keepAlivePersistence
        tplayer.Actor ! ResetAllEnvironmentInteractions
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if obj.Definition == GlobalDefinitions.vanu_sentry_turret =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.Name}")
        obj.Zone.LocalEvents ! LocalServiceMessage(obj.Zone.id, LocalAction.SetEmpire(obj.GUID, player.Faction))
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, seatNumber, _)
        if !obj.isUpgrading || System.currentTimeMillis() - GenericHackables.getTurretUpgradeTime >= 1500L =>
        obj.setMiddleOfUpgrade(false)
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.Name}")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: FacilityTurret, _, _) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.warn(
          s"MountVehicleMsg: ${tplayer.Name} wants to mount turret ${obj.GUID.guid}, but needs to wait until it finishes updating"
        )

      case Mountable.CanMount(obj: PlanetSideGameObject with FactionAffinity with WeaponTurret with InGameHistory, seatNumber, _) =>
        sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_mount")
        log.info(s"${player.Name} mounts the ${obj.Definition.asInstanceOf[BasicDefinition].Name}")
        sendResponse(PlanetsideAttributeMessage(obj.GUID, attribute_type=0, obj.Health))
        ops.updateWeaponAtSeatPosition(obj, seatNumber)
        MountingAction(tplayer, obj, seatNumber)

      case Mountable.CanMount(obj: Mountable, _, _) =>
        log.warn(s"MountVehicleMsg: $obj is some kind of mountable object but nothing will happen for ${player.Name}")

      case Mountable.CanDismount(obj: ImplantTerminalMech, seatNum, _) =>
        log.info(s"${tplayer.Name} dismounts the implant terminal")
        DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, _, mountPoint)
        if obj.Definition == GlobalDefinitions.orbital_shuttle && obj.MountedIn.nonEmpty =>
        //dismount to hart lobby
        val pguid = player.GUID
        log.info(s"${tplayer.Name} dismounts the orbital shuttle into the lobby")
        val sguid = obj.GUID
        val (pos, zang) = Vehicles.dismountShuttle(obj, mountPoint)
        tplayer.Position = pos
        sendResponse(DelayedPathMountMsg(pguid, sguid, u1=60, u2=true))
        continent.LocalEvents ! LocalServiceMessage(
          continent.id,
          LocalAction.SendResponse(ObjectDetachMessage(sguid, pguid, pos, roll=0, pitch=0, zang))
        )
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        //get ready for orbital drop
        val pguid = player.GUID
        val events = continent.VehicleEvents
        log.info(s"${player.Name} is prepped for dropping")
        DismountAction(tplayer, obj, seatNum)
        continent.actor ! ZoneActor.RemoveFromBlockMap(player) //character doesn't need it
        //DismountAction(...) uses vehicle service, so use that service to coordinate the remainder of the messages
        events ! VehicleServiceMessage(
          player.Name,
          VehicleAction.SendResponse(Service.defaultPlayerGUID, PlayerStasisMessage(pguid)) //the stasis message
        )
        //when the player dismounts, they will be positioned where the shuttle was when it disappeared in the sky
        //the player will fall to the ground and is perfectly vulnerable in this state
        //additionally, our player must exist in the current zone
        //having no in-game avatar target will throw us out of the map screen when deploying and cause softlock
        events ! VehicleServiceMessage(
          player.Name,
          VehicleAction.SendResponse(
            Service.defaultPlayerGUID,
            PlayerStateShiftMessage(ShiftState(unk=0, obj.Position, obj.Orientation.z, vel=None)) //cower in the shuttle bay
          )
        )
        events ! VehicleServiceMessage(
          continent.id,
          VehicleAction.SendResponse(pguid, GenericObjectActionMessage(pguid, code=9)) //conceal the player
        )
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.droppod =>
        log.info(s"${tplayer.Name} has landed on ${continent.id}")
        sessionLogic.general.unaccessContainer(obj)
        DismountAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID =>
        //disembarking self
        log.info(s"${player.Name} dismounts the ${obj.Definition.Name}'s ${
          obj.SeatPermissionGroup(seatNum) match {
            case Some(AccessPermissionGroup.Driver) => "driver seat"
            case Some(seatType) => s"$seatType seat (#$seatNum)"
            case None => "seat"
          }
        }")
        sessionLogic.vehicles.ConditionalDriverVehicleControl(obj)
        sessionLogic.general.unaccessContainer(obj)
        DismountVehicleAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seat_num, unk2=true, obj.GUID)
        )

      case Mountable.CanDismount(obj: PlanetSideGameObject with PlanetSideGameObject with Mountable with FactionAffinity with InGameHistory, seatNum, _) =>
        log.info(s"${tplayer.Name} dismounts a ${obj.Definition.asInstanceOf[ObjectDefinition].Name}")
        DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Mountable, _, _) =>
        log.warn(s"DismountVehicleMsg: $obj is some dismountable object but nothing will happen for ${player.Name}")

      case Mountable.CanNotMount(obj: Vehicle, seatNumber) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seatNumber, but was not allowed")
        obj.GetSeatFromMountPoint(seatNumber).collect {
          case seatNum if obj.SeatPermissionGroup(seatNum).contains(AccessPermissionGroup.Driver) =>
            sendResponse(
              ChatMsg(ChatMessageType.CMT_OPEN, wideContents=false, recipient="", "You are not the driver of this vehicle.", note=None)
            )
        }

      case Mountable.CanNotMount(obj: Mountable, seatNumber) =>
        log.warn(s"MountVehicleMsg: ${tplayer.Name} attempted to mount $obj's seat $seatNumber, but was not allowed")

      case Mountable.CanNotDismount(obj, seatNum) =>
        log.warn(s"DismountVehicleMsg: ${tplayer.Name} attempted to dismount $obj's mount $seatNum, but was not allowed")
    }
  }

  /* support functions */

  private def dismountWarning(
                               bailAs: BailType.Value,
                               kickedByDriver: Boolean
                             )
                             (
                               note: String,
                               player: Player
                             ): Unit = {
    log.warn(note)
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  private def dismountError(
                             bailAs: BailType.Value,
                             kickedByDriver: Boolean
                           )
                           (
                             note: String,
                             player: Player
                           ): Unit = {
    log.error(s"$note; some vehicle might not know that ${player.Name} is no longer sitting in it")
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  /**
   * Common activities/procedure when a player mounts a valid object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount into which the player is mounting
   */
  private def MountingAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    val objGuid: PlanetSideGUID    = obj.GUID
    sessionLogic.actionsToCancel()
    avatarActor ! AvatarActor.DeactivateActiveImplants()
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(3.seconds)
    sendResponse(ObjectAttachMessage(objGuid, playerGuid, seatNum))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.MountVehicle(playerGuid, objGuid, seatNum)
    )
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  private def DismountVehicleAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    DismountAction(tplayer, obj, seatNum)
    //until vehicles maintain synchronized momentum without a driver
    obj match {
      case v: Vehicle
        if seatNum == 0 && Vector3.MagnitudeSquared(v.Velocity.getOrElse(Vector3.Zero)) > 0f =>
        sessionLogic.vehicles.serverVehicleControlVelocity.collect { _ =>
          sessionLogic.vehicles.ServerVehicleOverrideStop(v)
        }
        v.Velocity = Vector3.Zero
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            tplayer.GUID,
            v.GUID,
            unk1 = 0,
            v.Position,
            v.Orientation,
            vel = None,
            v.Flying,
            unk3 = 0,
            unk4 = 0,
            wheel_direction = 15,
            unk5 = false,
            unk6 = v.Cloaked
          )
        )
      case _ => ()
    }
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  private def DismountAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    tplayer.ContributionFrom(obj)
    sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive
    val bailType = if (tplayer.BailProtection) {
      BailType.Bailed
    } else {
      BailType.Normal
    }
    sendResponse(DismountVehicleMsg(playerGuid, bailType, wasKickedByDriver = false))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.DismountVehicle(playerGuid, bailType, unk2 = false)
    )
  }
}
