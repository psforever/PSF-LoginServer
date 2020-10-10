// Copyright (c) 2017 PSForever
package net.psforever.services.local

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, Terminal}
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.packet.game.{TriggeredEffect, TriggeredEffectLocation}
import net.psforever.objects.vital.Vitality
import net.psforever.types.{PlanetSideGUID, Vector3}
import net.psforever.services.local.support._
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{GenericEventBus, RemoverActor, Service}

import scala.concurrent.duration._
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.services.support.SupportActor

import scala.concurrent.duration.Duration

class LocalService(zone: Zone) extends Actor {
  private val doorCloser   = context.actorOf(Props[DoorCloseActor](), s"${zone.id}-local-door-closer")
  private val hackClearer  = context.actorOf(Props[HackClearActor](), s"${zone.id}-local-hack-clearer")
  private val hackCapturer = context.actorOf(Props[HackCaptureActor](), s"${zone.id}-local-hack-capturer")
  private val engineer     = context.actorOf(Props(classOf[DeployableRemover], zone.tasks), s"${zone.id}-deployable-remover-agent")
  private val teleportDeployment: ActorRef =
    context.actorOf(Props[RouterTelepadActivation](), s"${zone.id}-telepad-activate-agent")
  private[this] val log = org.log4s.getLogger

  override def preStart() = {
    log.trace(s"Awaiting ${zone.id} local events ...")
  }

  val LocalEvents = new GenericEventBus[LocalServiceResponse]

  def receive: Receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/Local"
      val who  = sender()
      log.info(s"$who has joined $path")
      LocalEvents.subscribe(who, path)

    case Service.Leave(None) =>
      LocalEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Local"
      val who  = sender()
      log.info(s"$who has left $path")
      LocalEvents.unsubscribe(who, path)

    case Service.LeaveAll() =>
      LocalEvents.unsubscribe(sender())

    case LocalServiceMessage(forChannel, action) =>
      action match {
        case LocalAction.AlertDestroyDeployable(_, obj) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.AlertDestroyDeployable(obj)
            )
          )
        case LocalAction.DeployableMapIcon(player_guid, behavior, deployInfo) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.DeployableMapIcon(behavior, deployInfo)
            )
          )
        case LocalAction.Detonate(guid, obj) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", Service.defaultPlayerGUID, LocalResponse.Detonate(guid, obj))
          )
        case LocalAction.DoorOpens(player_guid, _, door) =>
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
        case LocalAction.HackTemporarily(player_guid, _, target, unk1, duration, unk2) =>
          hackClearer ! HackClearActor.ObjectIsHacked(target, zone, unk1, unk2, duration)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackObject(target.GUID, unk1, unk2))
          )
        case LocalAction.ClearTemporaryHack(_, target) =>
          hackClearer ! HackClearActor.ObjectIsResecured(target)
        case LocalAction.HackCaptureTerminal(player_guid, _, target, unk1, unk2, isResecured) =>
          // When a CC is hacked (or resecured) all amenities for the base should be unhacked
          val building = target.Owner.asInstanceOf[Building]
          val hackableAmenities =
            building.Amenities.filter(x => x.isInstanceOf[Hackable]).map(x => x.asInstanceOf[Amenity with Hackable])
          hackableAmenities.foreach(amenity =>
            if (amenity.HackedBy.isDefined) { hackClearer ! HackClearActor.ObjectIsResecured(amenity) }
          )

          if (isResecured) {
            hackCapturer ! HackCaptureActor.ClearHack(target, zone)
          } else {
            target.Definition match {
              case GlobalDefinitions.capture_terminal =>
                // Base CC
                hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration = 15 minutes)
              case GlobalDefinitions.secondary_capture =>
                // Tower CC
                hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration = 1 nanosecond)
              case GlobalDefinitions.vanu_control_console =>
                hackCapturer ! HackCaptureActor.ObjectIsHacked(target, zone, unk1, unk2, duration = 10 minutes)
            }
          }

          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.HackCaptureTerminal(target.GUID, unk1, unk2, isResecured)
            )
          )

          // If the owner of this capture terminal is on the lattice trigger a zone wide map update to update lattice benefits
          zone.Lattice find building match {
            case Some(_) => building.Zone.actor ! ZoneActor.ZoneMapUpdate()
            case None    => ;
          }
        case LocalAction.RouterTelepadTransport(player_guid, passenger_guid, src_guid, dest_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.RouterTelepadTransport(passenger_guid, src_guid, dest_guid)
            )
          )
        case LocalAction.SetEmpire(object_guid, empire) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.SetEmpire(object_guid, empire)
            )
          )
        case LocalAction.ToggleTeleportSystem(player_guid, router, system_plan) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.ToggleTeleportSystem(router, system_plan)
            )
          )
        case LocalAction.TriggerEffect(player_guid, effect, target) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.TriggerEffect(target, effect))
          )
        case LocalAction.TriggerEffectLocation(player_guid, effect, pos, orient) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.TriggerEffect(PlanetSideGUID(0), effect, None, Some(TriggeredEffectLocation(pos, orient)))
            )
          )
        case LocalAction.TriggerEffectInfo(player_guid, effect, target, unk1, unk2) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.TriggerEffect(target, effect, Some(TriggeredEffect(unk1, unk2)))
            )
          )
        case LocalAction.TriggerSound(player_guid, sound, pos, unk, volume) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.TriggerSound(sound, pos, unk, volume)
            )
          )
        case LocalAction.UpdateForceDomeStatus(player_guid, building_guid, activated) => {
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.UpdateForceDomeStatus(building_guid, activated)
            )
          )
        }
        case LocalAction.RechargeVehicleWeapon(player_guid, vehicle_guid, weapon_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.RechargeVehicleWeapon(vehicle_guid, weapon_guid)
            )
          )
        case _ => ;
      }

    //response from DoorCloseActor
    case DoorCloseActor.CloseTheDoor(door_guid, _) =>
      LocalEvents.publish(
        LocalServiceResponse(s"/${zone.id}/Local", Service.defaultPlayerGUID, LocalResponse.DoorCloses(door_guid))
      )

    //response from HackClearActor
    case HackClearActor.ClearTheHack(target_guid, _, unk1, unk2) =>
      log.info(s"Clearing hack for $target_guid")
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          Service.defaultPlayerGUID,
          LocalResponse.HackClear(target_guid, unk1, unk2)
        )
      )

    //message from ProximityTerminalControl
    case Terminal.StartProximityEffect(terminal) =>
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          PlanetSideGUID(0),
          LocalResponse.ProximityTerminalEffect(terminal.GUID, true)
        )
      )
    case Terminal.StopProximityEffect(terminal) =>
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          PlanetSideGUID(0),
          LocalResponse.ProximityTerminalEffect(terminal.GUID, false)
        )
      )

    case HackCaptureActor.HackTimeoutReached(capture_terminal_guid, _, _, _, hackedByFaction) =>
      val terminal = zone.GUID(capture_terminal_guid).get.asInstanceOf[CaptureTerminal]
      val building = terminal.Owner.asInstanceOf[Building]

      if (building.NtuLevel > 0) {
        log.info(s"Setting base ${building.GUID} / MapId: ${building.MapId} as owned by $hackedByFaction")
        building.Actor ! BuildingActor.SetFaction(hackedByFaction)
      } else {
        log.info("Base hack completed, but base was out of NTU.")
      }

      // FIXME shitty workaround so we don't get a "resecured by owner" message
      // SetFaction must be processed before we can keep going
      Thread.sleep(1000)

      // Reset CC back to normal operation
      self ! LocalServiceMessage(
        zone.id,
        LocalAction.HackCaptureTerminal(PlanetSideGUID(-1), zone, terminal, 0, 8L, isResecured = true)
      )
      //todo: this appears to be the way to reset the base warning lights after the hack finishes but it doesn't seem to work. The attribute above is a workaround
      self ! HackClearActor.ClearTheHack(building.GUID, zone.id, 3212836864L, 8L)

    //message to Engineer
    case LocalServiceMessage.Deployables(msg) =>
      engineer forward msg

    //message(s) from Engineer
    case msg @ DeployableRemover.EliminateDeployable(obj: TurretDeployable, guid, pos, _) =>
      val seats = obj.Seats.values
      if (seats.count(_.isOccupied) > 0) {
        val wasKickedByDriver = false //TODO yeah, I don't know
        seats.foreach(seat => {
          seat.Occupant match {
            case Some(tplayer) =>
              seat.Occupant = None
              tplayer.VehicleSeated = None
              zone.VehicleEvents ! VehicleServiceMessage(
                zone.id,
                VehicleAction.KickPassenger(tplayer.GUID, 4, wasKickedByDriver, obj.GUID)
              )
            case None => ;
          }
        })
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.scheduleOnce(Duration.create(2, "seconds"), self, msg)
      } else {
        EliminateDeployable(obj, guid, pos)
      }

    case DeployableRemover.EliminateDeployable(obj: BoomerDeployable, guid, pos, _) =>
      EliminateDeployable(obj, guid, pos)
      obj.Trigger match {
        case Some(trigger) =>
          log.warn(s"LocalService: deconstructing boomer in ${zone.id}, but trigger@${trigger.GUID.guid} still exists")
        case _ => ;
      }

    case DeployableRemover.EliminateDeployable(obj: TelepadDeployable, guid, pos, _) =>
      obj.Active = false
      //ClearSpecific will also remove objects that do not have GUID's; we may not have a GUID at this time
      teleportDeployment ! SupportActor.ClearSpecific(List(obj), zone)
      EliminateDeployable(obj, guid, pos)

    case DeployableRemover.EliminateDeployable(obj, guid, pos, _) =>
      EliminateDeployable(obj, guid, pos)

    case DeployableRemover.DeleteTrigger(trigger_guid, _) =>
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          Service.defaultPlayerGUID,
          LocalResponse.ObjectDelete(trigger_guid, 0)
        )
      )

    //message to RouterTelepadActivation
    case LocalServiceMessage.Telepads(msg) =>
      teleportDeployment forward msg

    //from RouterTelepadActivation
    case RouterTelepadActivation.ActivateTeleportSystem(telepad, _) =>
      val remoteTelepad = telepad.asInstanceOf[TelepadDeployable]
      remoteTelepad.Active = true
      zone.GUID(remoteTelepad.Router) match {
        case Some(router: Vehicle) =>
          router.Utility(UtilityType.internal_router_telepad_deployable) match {
            case Some(internalTelepad: Utility.InternalTelepad) =>
              //get rid of previous linked remote telepad (if any)
              zone.GUID(internalTelepad.Telepad) match {
                case Some(old: TelepadDeployable) =>
                  log.info(
                    s"ActivateTeleportSystem: old remote telepad@${old.GUID.guid} linked to internal@${internalTelepad.GUID.guid} will be deconstructed"
                  )
                  old.Active = false
                  engineer ! SupportActor.ClearSpecific(List(old), zone)
                  engineer ! RemoverActor.AddTask(old, zone, Some(0 seconds))
                case _ => ;
              }
              internalTelepad.Telepad = remoteTelepad.GUID
              if (internalTelepad.Active) {
                log.info(
                  s"ActivateTeleportSystem: fully deployed router@${router.GUID.guid} in ${zone.id} will link internal@${internalTelepad.GUID.guid} and remote@${remoteTelepad.GUID.guid}"
                )
                LocalEvents.publish(
                  LocalServiceResponse(
                    s"/${zone.id}/Local",
                    Service.defaultPlayerGUID,
                    LocalResponse.ToggleTeleportSystem(router, Some((internalTelepad, remoteTelepad)))
                  )
                )
              } else {
                remoteTelepad.OwnerName match {
                  case Some(name) =>
                    LocalEvents.publish(
                      LocalServiceResponse(
                        s"/$name/Local",
                        Service.defaultPlayerGUID,
                        LocalResponse.RouterTelepadMessage("@Teleport_NotDeployed")
                      )
                    )
                  case None => ;
                }
              }
            case _ =>
              log.error(s"ActivateTeleportSystem: vehicle@${router.GUID.guid} in ${zone.id} is not a router?")
              RouterTelepadError(remoteTelepad, "@Telepad_NoDeploy_RouterLost")
          }
        case Some(o) =>
          log.error(s"ActivateTeleportSystem: ${o.Definition.Name}@${o.GUID.guid} in ${zone.id} is not a router")
          RouterTelepadError(remoteTelepad, "@Telepad_NoDeploy_RouterLost")
        case None =>
          RouterTelepadError(remoteTelepad, "@Telepad_NoDeploy_RouterLost")
      }

    //synchronized damage calculations
    case Vitality.DamageOn(target: Deployable, damage_func) =>
      val cause = damage_func(target)
      sender() ! Vitality.DamageResolution(target, cause)

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }

  /**
    * na
    * @param telepad na
    * @param msg na
    */
  def RouterTelepadError(telepad: TelepadDeployable, msg: String): Unit = {
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
    */
  def EliminateDeployable(obj: PlanetSideGameObject with Deployable, guid: PlanetSideGUID, position: Vector3): Unit = {
    LocalEvents.publish(
      LocalServiceResponse(
        s"/${zone.id}/Local",
        Service.defaultPlayerGUID,
        LocalResponse.EliminateDeployable(obj, guid, position)
      )
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
