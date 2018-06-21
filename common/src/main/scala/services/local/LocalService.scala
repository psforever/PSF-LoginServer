// Copyright (c) 2017 PSForever
package services.local

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.zones.{InterstellarCluster, Zone}
import net.psforever.objects.zones.InterstellarCluster.GetWorld
import services.local.support.{DoorCloseActor, HackCaptureActor, HackClearActor}
import services.{GenericEventBus, Service, ServiceManager}
import services.local.support.{DoorCloseActor, HackClearActor}

import scala.util.Success
import scala.concurrent.duration._
import akka.pattern.ask
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.CaptureTerminal
import net.psforever.packet.game.PlanetSideGUID
import services.ServiceManager.Lookup

class LocalService extends Actor {
  private val doorCloser = context.actorOf(Props[DoorCloseActor], "local-door-closer")
  private val hackClearer = context.actorOf(Props[HackClearActor], "local-hack-clearer")
  private val hackCapturer = context.actorOf(Props[HackCaptureActor], "local-hack-capturer")
  private [this] val log = org.log4s.getLogger
  var cluster : ActorRef = Actor.noSender

  override def preStart = {
    log.info("Starting...")
    ServiceManager.serviceManager ! Lookup("cluster")
  }

  val LocalEvents = new GenericEventBus[LocalServiceResponse]

  def receive = {
    case ServiceManager.LookupResult("cluster", endpoint) =>
      cluster = endpoint
      log.trace("LocalService got cluster service " + endpoint)

    case Service.Join(channel) =>
      val path = s"/$channel/Local"
      val who = sender()
      log.info(s"$who has joined $path")
      LocalEvents.subscribe(who, path)

    case Service.Leave(None) =>
      LocalEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Local"
      val who = sender()
      log.info(s"$who has left $path")
      LocalEvents.unsubscribe(who, path)

    case Service.LeaveAll() =>
      LocalEvents.unsubscribe(sender())

    case LocalServiceMessage(forChannel, action) =>
      action match {
        case LocalAction.DoorOpens(player_guid, zone, door) =>
          doorCloser ! DoorCloseActor.DoorIsOpen(door, zone)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.DoorOpens(door.GUID))
          )
        case LocalAction.DoorCloses(player_guid, door_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.DoorCloses(door_guid))
          )
        case LocalAction.HackClear(player_guid, target, unk1, unk2) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackClear(target.GUID, unk1, unk2))
          )
        case LocalAction.HackTemporarily(player_guid, zone, target, unk1, duration, unk2) =>
          hackClearer ! HackClearActor.ObjectIsHacked(target, zone, unk1, unk2, duration)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackObject(target.GUID, unk1, unk2))
          )
        case LocalAction.ClearTemporaryHack(player_guid, target) =>
          hackClearer ! HackClearActor.ObjectIsResecured(target)
        case LocalAction.HackCaptureTerminal(player_guid, zone, target, unk1, unk2, isResecured) =>

          if(isResecured){
            hackCapturer ! HackCaptureActor.ClearHack(target, zone)
          } else {
            hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2)
          }

          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackCaptureTerminal(target.GUID, unk1, unk2, isResecured))
          )
          //todo: publish to galaxy service for map update
        case LocalAction.ProximityTerminalEffect(player_guid, object_guid, effectState) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.ProximityTerminalEffect(object_guid, effectState))
          )
        case LocalAction.TriggerSound(player_guid, sound, pos, unk, volume) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerSound(sound, pos, unk, volume))
          )
        case LocalAction.SetEmpire(object_guid, empire) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", PlanetSideGUID(-1), LocalResponse.SetEmpire(object_guid, empire))
          )
        case _ => ;
      }

    //response from DoorCloseActor
    case DoorCloseActor.CloseTheDoor(door_guid, zone_id) =>
      LocalEvents.publish(
        LocalServiceResponse(s"/$zone_id/Local", Service.defaultPlayerGUID, LocalResponse.DoorCloses(door_guid))
      )

    //response from HackClearActor
    case HackClearActor.ClearTheHack(target_guid, zone_id, unk1, unk2) =>
      log.warn(s"Clearing hack for ${target_guid}")
      LocalEvents.publish(
        LocalServiceResponse(s"/$zone_id/Local", Service.defaultPlayerGUID, LocalResponse.HackClear(target_guid, unk1, unk2))
      )

    case HackCaptureActor.HackTimeoutReached(capture_terminal_guid, zone_id, unk1, unk2, hackedByFaction) =>
      import scala.concurrent.ExecutionContext.Implicits.global
      ask(cluster, InterstellarCluster.GetWorld(zone_id))(1 seconds).onComplete {
          case Success(InterstellarCluster.GiveWorld(zoneId, zone)) =>
            val terminal = zone.asInstanceOf[Zone].GUID(capture_terminal_guid).get.asInstanceOf[CaptureTerminal]
            val building = terminal.Owner.asInstanceOf[Building]

            // todo: Move this to a function for Building
            var ntuLevel = 0
            building.Amenities.filter(x => (x.Definition == GlobalDefinitions.resource_silo)).headOption.asInstanceOf[Option[ResourceSilo]] match {
              case Some(obj: ResourceSilo) =>
                ntuLevel = obj.CapacitorDisplay.toInt
              case _ => ;
            }

            if(ntuLevel > 0) {
              log.info(s"Setting base ${building.ModelId} as owned by ${hackedByFaction}")

              building.Faction = hackedByFaction
              self ! LocalServiceMessage(zone.Id, LocalAction.SetEmpire(PlanetSideGUID(building.ModelId), hackedByFaction))

              self ! LocalServiceMessage(zone.Id, LocalAction.HackCaptureTerminal(PlanetSideGUID(-1), zone, terminal, 0, 8L, isResecured = true))
              //todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work. The attribute above is a workaround
              self ! HackClearActor.ClearTheHack(PlanetSideGUID(building.ModelId), zone.Id, 3212836864L, 8L)
            } else {
              log.info("Base hack completed, but base was out of NTU.")
            }
          case Success(_) =>
            log.warn("Got success from InterstellarCluster.GetWorld but didn't know how to handle it")

        case scala.util.Failure(_) => log.warn(s"LocalService Failed to get zone when hack timeout was reached")
      }
    case HackCaptureActor.GetHackTimeRemainingNanos(capture_console_guid) =>
      hackCapturer forward HackCaptureActor.GetHackTimeRemainingNanos(capture_console_guid)
    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
