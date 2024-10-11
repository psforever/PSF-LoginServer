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
import net.psforever.objects.vital.InGameHistory
import net.psforever.packet.game.{DelayedPathMountMsg, DismountVehicleCargoMsg, DismountVehicleMsg, GenericObjectActionMessage, MountVehicleCargoMsg, MountVehicleMsg, ObjectDetachMessage, PlayerStasisMessage, PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

object MountHandlerLogic {
  def apply(ops: SessionMountHandlers): MountHandlerLogic = {
    new MountHandlerLogic(ops, ops.context)
  }
}

class MountHandlerLogic(val ops: SessionMountHandlers, implicit val context: ActorContext) extends MountHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  /* packets */

  def handleMountVehicle(pkt: MountVehicleMsg): Unit = { /* can not mount as spectator */ }

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit = {
    ops.handleDismountVehicle(pkt)
  }

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = { /* can not mount as spectator */ }

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit = {
    ops.handleDismountVehicleCargo(pkt)
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
        ops.DismountAction(tplayer, obj, seatNum)
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
        ops.DismountAction(tplayer, obj, seatNum)
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
        ops.DismountAction(tplayer, obj, seatNum)
        obj.Actor ! Vehicle.Deconstruct()

      case Mountable.CanDismount(obj: Vehicle, seatNum, _)
        if tplayer.GUID == player.GUID =>
        sessionLogic.vehicles.ConditionalDriverVehicleControl(obj)
        sessionLogic.general.unaccessContainer(obj)
        ops.DismountVehicleAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(obj: Vehicle, seat_num, _) =>
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.KickPassenger(tplayer.GUID, seat_num, unk2=true, obj.GUID)
        )

      case Mountable.CanDismount(obj: PlanetSideGameObject with PlanetSideGameObject with Mountable with FactionAffinity with InGameHistory, seatNum, _) =>
        ops.DismountAction(tplayer, obj, seatNum)

      case Mountable.CanDismount(_: Mountable, _, _) => ()

      case Mountable.CanNotDismount(obj: Vehicle, _, _) =>
        obj.Actor ! Vehicle.Deconstruct()

      case _ => ()
    }
  }

  /* support functions */
}
