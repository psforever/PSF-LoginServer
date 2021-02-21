// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.pad.shuttle

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.time.ShuttleTimer
import net.psforever.services.{Service, ServiceManager}
import net.psforever.types.{ChatMessageType, Vector3}

class OrbitalShuttlePadControl(pad: OrbitalShuttlePad) extends Actor {
  var managedDoors: List[Door] = Nil

  def receive: Receive = startUp

  val taxiing: Receive = {
    case ShuttleTimer.LockDoors =>
      managedDoors.foreach { door =>
        door.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.lockedWaitingForShuttle)
        val zone = pad.Zone
        if(door.isOpen) {
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorSlamsShut(door))
        }
      }

    case ShuttleTimer.UnlockDoors =>
      managedDoors.foreach { _.Actor ! Door.ResetMechanism }

    case ShuttleTimer.ShuttleDocked =>
      val zone = pad.Zone
      zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleDock(pad.GUID, pad.shuttle.GUID, 3))

    case ShuttleTimer.ShuttleFreeFromDock =>
      val zone = pad.Zone
      val shuttle = pad.shuttle
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.ShuttleUndock(pad.GUID, shuttle.GUID, shuttle.Position, shuttle.Orientation)
      )

    case ShuttleTimer.ShuttleStateUpdate(state) =>
      val zone = pad.Zone
      val shuttle = pad.shuttle
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.ShuttleState(shuttle.GUID, shuttle.Position, shuttle.Orientation, state)
      )

    case _ => ;
  }

  val shuttleTime: Receive = {
    case ServiceManager.LookupResult("shuttleTimer", timer) =>
      timer ! ShuttleTimer.PairWith(pad.Zone, pad.GUID, pad.shuttle.GUID, self)
      context.become(taxiing)

    case _ => ;
  }

  val startUp: Receive = {
    case Service.Startup() if pad.shuttle.HasGUID =>
      val position = pad.Position
      pad.shuttle.Position = position + Vector3(0,8.25f,0) //magic offset
      pad.shuttle.Orientation = pad.Orientation
      pad.shuttle.Faction = pad.Faction
      pad.Zone.Transport ! Zone.Vehicle.Spawn(pad.shuttle)
      ServiceManager.serviceManager ! ServiceManager.Lookup("shuttleTimer")

      managedDoors = pad.Owner.Amenities
        .collect { case d: Door if d.Definition == GlobalDefinitions.gr_door_mb_orb => d }
        .sortBy { o => Vector3.DistanceSquared(position, o.Position) }
        .take(8)
      context.become(shuttleTime)

    case _ => ;
  }
}

object OrbitalShuttlePadControl {
  def lockedWaitingForShuttle(obj: PlanetSideServerObject, door: Door): Boolean = {
    val zone = door.Zone
    val channel = obj match {
      case p: Player => p.Name
      case _ => ""
    }
    zone.AvatarEvents ! AvatarServiceMessage(
      channel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ChatMsg(ChatMessageType.UNK_225, false, "", "@DoorWillOpenWhenShuttleReturns", None)
      )
    )
    false
  }
}
