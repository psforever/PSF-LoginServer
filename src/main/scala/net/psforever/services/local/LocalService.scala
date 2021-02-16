// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.objects._
import akka.actor.{Actor, Props}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{TriggeredEffect, TriggeredEffectLocation}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.support.SupportActor
import net.psforever.services.local.support._
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.services.{GenericEventBus, RemoverActor, Service}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.concurrent.duration.{Duration, _}

class LocalService(zone: Zone) extends Actor {
  private val doorCloser = context.actorOf(
    Props[DoorCloseActor](), s"${zone.id}-local-door-closer"
  )
  private val hackClearer = context.actorOf(
    Props[HackClearActor](), s"${zone.id}-local-hack-clearer"
  )
  private val hackCapturer = context.actorOf(
    Props(classOf[HackCaptureActor], zone.tasks), s"${zone.id}-local-hack-capturer"
  )
  private val captureFlagManager = context.actorOf(
    Props(classOf[CaptureFlagManager], zone.tasks, zone), s"${zone.id}-local-capture-flag-manager"
  )
  private val engineer = context.actorOf(
    Props(classOf[DeployableRemover], zone.tasks), s"${zone.id}-deployable-remover-agent"
  )

  private[this] val log = org.log4s.getLogger

  val LocalEvents = new GenericEventBus[LocalServiceResponse]

  def receive: Receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/Local"
      LocalEvents.subscribe(sender(), path)

    case Service.Leave(None) =>
      LocalEvents.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = s"/$channel/Local"
      LocalEvents.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      LocalEvents.unsubscribe(sender())

    case LocalServiceMessage(forChannel, action) =>
      action match {
        case LocalAction.AlertBuildDeployable(obj, tool) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.AlertBuildDeployable(obj, tool)
            )
          )
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
        case LocalAction.DoorSlamsShut(door) =>
          val door_guid = door.GUID
          doorCloser ! SupportActor.HurrySpecific(List(door), zone)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", Service.defaultPlayerGUID, LocalResponse.DoorCloses(door_guid))
          )
        case LocalAction.HackClear(player_guid, target, unk1, unk2) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendHackMessageHackCleared(target.GUID, unk1, unk2)
            )
          )
        case LocalAction.HackTemporarily(player_guid, _, target, unk1, duration, unk2) =>
          hackClearer ! HackClearActor.ObjectIsHacked(target, zone, unk1, unk2, duration)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackObject(target.GUID, unk1, unk2))
          )
        case LocalAction.ClearTemporaryHack(_, target) =>
          hackClearer ! HackClearActor.ObjectIsResecured(target)

        case LocalAction.ResecureCaptureTerminal(target) =>
          hackCapturer ! HackCaptureActor.ResecureCaptureTerminal(target, zone)
        case LocalAction.StartCaptureTerminalHack(target) =>
          hackCapturer ! HackCaptureActor.StartCaptureTerminalHack(target, zone, 0, 8L)
        case LocalAction.LluCaptured(llu) =>
          hackCapturer ! HackCaptureActor.FlagCaptured(llu)

        case LocalAction.LluSpawned(player_guid, llu) =>
          // Forward to all clients to create object locally
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.LluSpawned(llu)
            )
          )

        case LocalAction.LluDespawned(player_guid, llu) =>
          // Forward to all clients to destroy object locally
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.LluDespawned(llu)
            )
          )

        case LocalAction.SendPacket(packet) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              PlanetSideGUID(-1),
              LocalResponse.SendPacket(packet)
            )
          )

        case LocalAction.SendPlanetsideAttributeMessage(player_guid, target_guid, attribute_number, attribute_value) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendPlanetsideAttributeMessage(target_guid, attribute_number, attribute_value)
            )
          )
        case LocalAction.SendGenericObjectActionMessage(player_guid, target_guid, action_number) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendGenericObjectActionMessage(target_guid, action_number)
            )
          )

        case LocalAction.SendChatMsg(player_guid, msg) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendChatMsg(msg)
            )
          )

        case LocalAction.SendGenericActionMessage(player_guid, action_number) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendGenericActionMessage(action_number)
            )
          )
        case LocalAction.RouterTelepadMessage(msg) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.RouterTelepadMessage(msg)
            )
          )
        case LocalAction.RouterTelepadTransport(player_guid, passenger_guid, src_guid, dest_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.RouterTelepadTransport(passenger_guid, src_guid, dest_guid)
            )
          )
        case LocalAction.SendResponse(pkt) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.SendResponse(pkt)
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
        case LocalAction.ShuttleDock(pad, shuttle, slot) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.ShuttleDock(pad, shuttle, slot)
            )
          )
        case LocalAction.ShuttleUndock(pad, shuttle, pos, orient) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.ShuttleUndock(pad, shuttle, pos, orient)
            )
          )
        case LocalAction.ShuttleEvent(ev) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", Service.defaultPlayerGUID, LocalResponse.ShuttleEvent(ev))
          )
        case LocalAction.ShuttleState(guid, pos, orient, state) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.ShuttleState(guid, pos, orient, state)
            )
          )
        case LocalAction.StartRouterInternalTelepad(router_guid, obj_guid, obj) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.StartRouterInternalTelepad(router_guid, obj_guid, obj)
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
        case LocalAction.UpdateForceDomeStatus(player_guid, building_guid, activated) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.UpdateForceDomeStatus(building_guid, activated)
            )
          )
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
    case HackClearActor.SendHackMessageHackCleared(target_guid, _, unk1, unk2) =>
      log.info(s"Clearing hack for $target_guid")
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          Service.defaultPlayerGUID,
          LocalResponse.SendHackMessageHackCleared(target_guid, unk1, unk2)
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

    //message to Engineer
    case LocalServiceMessage.Deployables(msg) =>
      engineer forward msg

    //message(s) from Engineer
    case msg @ DeployableRemover.EliminateDeployable(obj: TurretDeployable, guid, pos, _) =>
      val seats = obj.Seats.values
      if (seats.count(_.isOccupied) > 0) {
        val wasKickedByDriver = false //TODO yeah, I don't know
        seats.foreach(seat => {
          seat.occupant match {
            case Some(tplayer) =>
              seat.unmount(tplayer)
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
                  log.trace(
                    s"ActivateTeleportSystem: old remote telepad@${old.GUID.guid} linked to internal@${internalTelepad.GUID.guid} will be deconstructed"
                  )
                  old.Active = false
                  engineer ! SupportActor.ClearSpecific(List(old), zone)
                  engineer ! RemoverActor.AddTask(old, zone, Some(0 seconds))
                case _ => ;
              }
              internalTelepad.Telepad = remoteTelepad.GUID
              if (internalTelepad.Active) {
                log.trace(
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

    // Forward all CaptureFlagManager messages
    case msg @ (CaptureFlagManager.SpawnCaptureFlag(_, _, _) | CaptureFlagManager.PickupFlag(_, _) |
        CaptureFlagManager.DropFlag(_) | CaptureFlagManager.Captured(_) | CaptureFlagManager.Lost(_, _) |
        CaptureFlagManager) =>
      captureFlagManager.forward(msg)

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
  def EliminateDeployable(obj: Deployable, guid: PlanetSideGUID, position: Vector3): Unit = {
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
