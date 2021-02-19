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
        door.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.lockedWaitingForShuttle _)
        val zone = pad.Zone
        if(door.isOpen) {
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorSlamsShut(door))
        }
      }

    case ShuttleTimer.UnlockDoors =>
      managedDoors.foreach { _.Actor ! Door.UpdateMechanism(None) }

    case _ => ;
  }

  val shuttleTime: Receive = {
    case ServiceManager.LookupResult("shuttleTimer", timer) =>
      timer ! ShuttleTimer.PairWith(pad.Zone, pad.GUID, pad.shuttle.GUID)
      context.become(taxiing)

    case _ => ;
  }

  val startUp: Receive = {
    case Service.Startup() if pad.shuttle.HasGUID =>
      val zone = pad.Zone
      pad.shuttle.Faction = pad.Faction
      pad.shuttle.Zone = zone
      zone.Transport ! Zone.Vehicle.Spawn(pad.shuttle)
      ServiceManager.serviceManager ! ServiceManager.Lookup("shuttleTimer")

      val position = pad.Position
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
