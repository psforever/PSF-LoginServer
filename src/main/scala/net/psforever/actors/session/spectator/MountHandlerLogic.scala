// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.SessionActor
import net.psforever.actors.session.normal.NormalMode
import net.psforever.actors.session.support.{MountHandlerFunctions, SessionData, SessionMountHandlers}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Vehicle, Vehicles}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMech
import net.psforever.objects.vehicles.{AccessPermissionGroup, CargoBehavior}
import net.psforever.objects.vital.InGameHistory
import net.psforever.packet.game.{DelayedPathMountMsg, DismountVehicleCargoMsg, DismountVehicleMsg, GenericObjectActionMessage, MountVehicleCargoMsg, MountVehicleMsg, ObjectDetachMessage, PlayerStasisMessage, PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, PlanetSideGUID, Vector3}

object MountHandlerLogic {
  def apply(ops: SessionMountHandlers): MountHandlerLogic = {
    new MountHandlerLogic(ops, ops.context)
  }
}

class MountHandlerLogic(val ops: SessionMountHandlers, implicit val context: ActorContext) extends MountHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  /* packets */

  def handleMountVehicle(pkt: MountVehicleMsg): Unit = { /* intentionally blank */ }

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
                case _ => ;
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

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = { /* intentionally blank */ }

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
      case Mountable.CanDismount(obj: ImplantTerminalMech, seatNum, _) =>
        DismountAction(tplayer, obj, seatNum)
        obj.Zone.actor ! ZoneActor.RemoveFromBlockMap(player)

      case Mountable.CanDismount(obj: Vehicle, _, mountPoint)
        if obj.Definition == GlobalDefinitions.orbital_shuttle && obj.MountedIn.nonEmpty =>
        //dismount to hart lobby
        val pguid = player.GUID
        val sguid = obj.GUID
        val (pos, zang) = Vehicles.dismountShuttle(obj, mountPoint)
        tplayer.Position = pos
        sendResponse(DelayedPathMountMsg(pguid, sguid, u1=60, u2=true))
        continent.LocalEvents ! LocalServiceMessage(
          continent.id,
          LocalAction.SendResponse(ObjectDetachMessage(sguid, pguid, pos, roll=0, pitch=0, zang))
        )
        obj.Zone.actor ! ZoneActor.RemoveFromBlockMap(player)
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.orbital_shuttle =>
        //get ready for orbital drop
        val pguid = player.GUID
        val events = continent.VehicleEvents
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
        context.self ! SessionActor.SetMode(NormalMode)
        sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if obj.Definition == GlobalDefinitions.droppod =>
        sessionLogic.general.unaccessContainer(obj)
        DismountAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID =>
        sessionLogic.vehicles.ConditionalDriverVehicleControl(obj)
        sessionLogic.general.unaccessContainer(obj)
        DismountVehicleAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seat_num, unk2=true, obj.GUID)
        )

      case Mountable.CanDismount(obj: PlanetSideGameObject with PlanetSideGameObject with Mountable with FactionAffinity with InGameHistory, seatNum, _) =>
        DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Mountable, _, _) => ()

      case Mountable.CanNotDismount(obj: Vehicle, seatNum) =>
        obj.Actor ! Vehicle.Deconstruct()

      case _ => ()
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
        v.Zone.actor ! ZoneActor.RemoveFromBlockMap(player)
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
