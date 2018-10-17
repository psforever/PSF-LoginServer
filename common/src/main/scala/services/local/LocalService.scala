// Copyright (c) 2017 PSForever
package services.local

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.CaptureTerminal
import net.psforever.objects.zones.{InterstellarCluster, Zone}
import net.psforever.objects._
import net.psforever.packet.game.{PlanetSideGUID, TriggeredEffect, TriggeredEffectLocation}
import net.psforever.objects.vital.Vitality
import net.psforever.types.Vector3
import services.local.support._
import services.vehicle.{VehicleAction, VehicleServiceMessage}
import services.{GenericEventBus, RemoverActor, Service, ServiceManager}

import scala.util.Success
import scala.concurrent.duration._
import akka.pattern.ask
import net.psforever.objects.vehicles.{Utility, UtilityType}
import services.ServiceManager.Lookup
import services.support.SupportActor

import scala.concurrent.duration.Duration

class LocalService extends Actor {
  private val doorCloser = context.actorOf(Props[DoorCloseActor], "local-door-closer")
  private val hackClearer = context.actorOf(Props[HackClearActor], "local-hack-clearer")
  private val hackCapturer = context.actorOf(Props[HackCaptureActor], "local-hack-capturer")
  private val engineer = context.actorOf(Props[DeployableRemover], "deployable-remover-agent")
  private val teleportDeployment : ActorRef = context.actorOf(Props[RouterTelepadActivation], "telepad-activate-agent")
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
        case LocalAction.AlertDestroyDeployable(_, obj) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", Service.defaultPlayerGUID, LocalResponse.AlertDestroyDeployable(obj))
          )
        case LocalAction.DeployableMapIcon(player_guid, behavior, deployInfo) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.DeployableMapIcon(behavior, deployInfo))
          )
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
        case LocalAction.ClearTemporaryHack(_, target) =>
          hackClearer ! HackClearActor.ObjectIsResecured(target)
        case LocalAction.HackCaptureTerminal(player_guid, zone, target, unk1, unk2, isResecured) =>

          if(isResecured){
            hackCapturer ! HackCaptureActor.ClearHack(target, zone)
          } else {
            target.Definition match {
              case GlobalDefinitions.capture_terminal =>
                // Base CC
                hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration = 15 minutes)
              case GlobalDefinitions.secondary_capture =>
                // Tower CC
                hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration = 1 nanosecond)
            }
          }

          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackCaptureTerminal(target.GUID, unk1, unk2, isResecured))
          )
        case LocalAction.ProximityTerminalEffect(player_guid, object_guid, effectState) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.ProximityTerminalEffect(object_guid, effectState))
          )
        case LocalAction.RouterTelepadTransport(player_guid, passenger_guid, src_guid, dest_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.RouterTelepadTransport(passenger_guid, src_guid, dest_guid))
          )
        case LocalAction.SetEmpire(object_guid, empire) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", Service.defaultPlayerGUID, LocalResponse.SetEmpire(object_guid, empire))
          )
        case LocalAction.ToggleTeleportSystem(player_guid, router, system_plan) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.ToggleTeleportSystem(router, system_plan))
          )
        case LocalAction.TriggerEffect(player_guid, effect, target) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerEffect(target, effect))
          )
        case LocalAction.TriggerEffectLocation(player_guid, effect, pos, orient) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerEffect(PlanetSideGUID(0), effect, None, Some(TriggeredEffectLocation(pos, orient))))
          )
        case LocalAction.TriggerEffectInfo(player_guid, effect, target, unk1, unk2) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerEffect(target, effect, Some(TriggeredEffect(unk1, unk2))))
          )
        case LocalAction.TriggerSound(player_guid, sound, pos, unk, volume) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerSound(sound, pos, unk, volume))
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
      log.warn(s"Clearing hack for $target_guid")
      LocalEvents.publish(
        LocalServiceResponse(s"/$zone_id/Local", Service.defaultPlayerGUID, LocalResponse.HackClear(target_guid, unk1, unk2))
      )

    case HackCaptureActor.HackTimeoutReached(capture_terminal_guid, zone_id, _, _, hackedByFaction) =>
      import scala.concurrent.ExecutionContext.Implicits.global
      ask(cluster, InterstellarCluster.GetWorld(zone_id))(1 seconds).onComplete {
          case Success(InterstellarCluster.GiveWorld(_, zone)) =>
            val terminal = zone.asInstanceOf[Zone].GUID(capture_terminal_guid).get.asInstanceOf[CaptureTerminal]
            val building = terminal.Owner.asInstanceOf[Building]

            // todo: Move this to a function for Building
            var ntuLevel = 0
            building.Amenities.find(_.Definition == GlobalDefinitions.resource_silo).asInstanceOf[Option[ResourceSilo]] match {
              case Some(obj: ResourceSilo) =>
                ntuLevel = obj.CapacitorDisplay.toInt
              case _ =>
                // Base has no NTU silo - likely a tower / cavern CC
                ntuLevel = 1
            }

            if(ntuLevel > 0) {
              log.info(s"Setting base ${building.ModelId} as owned by $hackedByFaction")

              building.Faction = hackedByFaction
              self ! LocalServiceMessage(zone.Id, LocalAction.SetEmpire(PlanetSideGUID(building.ModelId), hackedByFaction))
            } else {
              log.info("Base hack completed, but base was out of NTU.")
            }

            // Reset CC back to normal operation
            self ! LocalServiceMessage(zone.Id, LocalAction.HackCaptureTerminal(PlanetSideGUID(-1), zone, terminal, 0, 8L, isResecured = true))
            //todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work. The attribute above is a workaround
            self ! HackClearActor.ClearTheHack(PlanetSideGUID(building.ModelId), zone.Id, 3212836864L, 8L)
          case Success(_) =>
            log.warn("Got success from InterstellarCluster.GetWorld but didn't know how to handle it")

        case scala.util.Failure(_) => log.warn(s"LocalService Failed to get zone when hack timeout was reached")
      }

    case HackCaptureActor.GetHackTimeRemainingNanos(capture_console_guid) =>
      hackCapturer forward HackCaptureActor.GetHackTimeRemainingNanos(capture_console_guid)

    //message to Engineer
    case LocalServiceMessage.Deployables(msg) =>
      engineer forward msg

    //message(s) from Engineer
    case msg @ DeployableRemover.EliminateDeployable(obj : TurretDeployable, guid, pos, zone) =>
      val seats = obj.Seats.values
      if(seats.count(_.isOccupied) > 0) {
        val wasKickedByDriver = false //TODO yeah, I don't know
        seats.foreach(seat => {
          seat.Occupant match {
            case Some(tplayer) =>
              seat.Occupant = None
              tplayer.VehicleSeated = None
              zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.KickPassenger(tplayer.GUID, 4, wasKickedByDriver, obj.GUID))
            case None => ;
          }
        })
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(Duration.create(2, "seconds"), self, msg)
      }
      else {
        EliminateDeployable(obj, guid, pos, zone.Id)
      }

    case DeployableRemover.EliminateDeployable(obj : BoomerDeployable, guid, pos, zone) =>
      EliminateDeployable(obj, guid, pos, zone.Id)
      obj.Trigger match {
        case Some(trigger) =>
          log.warn(s"LocalService: deconstructing boomer in ${zone.Id}, but trigger@${trigger.GUID.guid} still exists")
        case _ => ;
      }

    case DeployableRemover.EliminateDeployable(obj : TelepadDeployable, guid, pos, zone) =>
      obj.Active = false
      //ClearSpecific will also remove objects that do not have GUID's; we may not have a GUID at this time
      teleportDeployment ! SupportActor.ClearSpecific(List(obj), zone)
      EliminateDeployable(obj, guid, pos, zone.Id)

    case DeployableRemover.EliminateDeployable(obj, guid, pos, zone) =>
      EliminateDeployable(obj, guid, pos, zone.Id)

    case DeployableRemover.DeleteTrigger(trigger_guid, zone) =>
      LocalEvents.publish(
        LocalServiceResponse(s"/${zone.Id}/Local", Service.defaultPlayerGUID, LocalResponse.ObjectDelete(trigger_guid, 0))
      )

    //message to RouterTelepadActivation
    case LocalServiceMessage.Telepads(msg) =>
      teleportDeployment forward msg

    //from RouterTelepadActivation
    case RouterTelepadActivation.ActivateTeleportSystem(telepad, zone) =>
      val remoteTelepad = telepad.asInstanceOf[TelepadDeployable]
      remoteTelepad.Active = true
      zone.GUID(remoteTelepad.Router) match {
        case Some(router : Vehicle) =>
          router.Utility(UtilityType.internal_router_telepad_deployable) match {
            case Some(internalTelepad : Utility.InternalTelepad) =>
              //get rid of previous linked remote telepad (if any)
              zone.GUID(internalTelepad.Telepad) match {
                case Some(old : TelepadDeployable) =>
                  log.info(s"ActivateTeleportSystem: old remote telepad@${old.GUID.guid} linked to internal@${internalTelepad.GUID.guid} will be deconstructed")
                  old.Active = false
                  engineer ! SupportActor.ClearSpecific(List(old), zone)
                  engineer ! RemoverActor.AddTask(old, zone, Some(0 seconds))
                case _ => ;
              }
              internalTelepad.Telepad = remoteTelepad.GUID
              if(internalTelepad.Active) {
                log.info(s"ActivateTeleportSystem: fully deployed router@${router.GUID.guid} in ${zone.Id} will link internal@${internalTelepad.GUID.guid} and remote@${remoteTelepad.GUID.guid}")
                LocalEvents.publish(
                  LocalServiceResponse(s"/${zone.Id}/Local", Service.defaultPlayerGUID, LocalResponse.ToggleTeleportSystem(router, Some((internalTelepad, remoteTelepad))))
                )
              }
              else {
                remoteTelepad.OwnerName match {
                  case Some(name) =>
                    LocalEvents.publish(
                      LocalServiceResponse(s"/$name/Local", Service.defaultPlayerGUID, LocalResponse.RouterTelepadMessage("@Teleport_NotDeployed"))
                    )
                  case None => ;
                }
              }
            case _ =>
              log.error(s"ActivateTeleportSystem: vehicle@${router.GUID.guid} in ${zone.Id} is not a router?")
              RouterTelepadError(remoteTelepad, zone, "@Telepad_NoDeploy_RouterLost")
          }
        case Some(o) =>
          log.error(s"ActivateTeleportSystem: ${o.Definition.Name}@${o.GUID.guid} in ${zone.Id} is not a router")
          RouterTelepadError(remoteTelepad, zone, "@Telepad_NoDeploy_RouterLost")
        case None =>
          RouterTelepadError(remoteTelepad, zone, "@Telepad_NoDeploy_RouterLost")
      }

    //synchronized damage calculations
    case Vitality.DamageOn(target : Deployable, func) =>
      func(target)
      sender ! Vitality.DamageResolution(target)

    case msg =>
      log.warn(s"Unhandled message $msg from $sender")
  }

  /**
    * na
    * @param telepad na
    * @param zone na
    * @param msg na
    */
  def RouterTelepadError(telepad : TelepadDeployable, zone : Zone, msg : String) : Unit = {
    telepad.OwnerName match {
      case Some(name) =>
        LocalEvents.publish(
          LocalServiceResponse(s"/$name/Local", Service.defaultPlayerGUID, LocalResponse.RouterTelepadMessage(msg))
        )
      case None => ;
    }
    engineer ! SupportActor.ClearSpecific(List(telepad), zone)
    engineer ! RemoverActor.AddTask(telepad, zone, Some(0 seconds))
  }

  /**
    * Common behavior for distributing information about a deployable's destruction or deconstruction.<br>
    * <br>
    * The primary distribution task instructs all clients to eliminate the target deployable.
    * This is a cosmetic exercise as the deployable should already be unregistered from its zone and
    * functionally removed from its zone's list of deployable objects by external operations.
    * The other distribution is a targeted message sent to the former owner of the deployable
    * if he still exists on the server
    * to clean up any leftover ownership-specific knowledge about the deployable.
    * @see `DeployableRemover`
    * @param obj the deployable object
    * @param guid the deployable objects globally unique identifier;
    *             may be a former identifier
    * @param position the deployable's position
    * @param zoneId the zone where the deployable is currently placed
    */
  def EliminateDeployable(obj : PlanetSideGameObject with Deployable, guid : PlanetSideGUID, position : Vector3, zoneId : String) : Unit = {
    LocalEvents.publish(
      LocalServiceResponse(s"/$zoneId/Local", Service.defaultPlayerGUID, LocalResponse.EliminateDeployable(obj, guid, position))
    )
    obj.OwnerName match {
      case Some(name) =>
        LocalEvents.publish(
          LocalServiceResponse(s"/$name/Local", Service.defaultPlayerGUID, LocalResponse.AlertDestroyDeployable(obj))
        )
      case None => ;
    }
  }
}
