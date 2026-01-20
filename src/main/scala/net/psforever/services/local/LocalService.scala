// Copyright (c) 2017 PSForever
package net.psforever.services.local

import akka.actor.{Actor, Props}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ObjectCreateMessage, TriggeredEffect, TriggeredEffectLocation}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.types.PlanetSideGUID
import net.psforever.services.local.support._
import net.psforever.services.Service
import net.psforever.services.base.GenericEventBus
import net.psforever.services.base.support.SupportActor

class LocalService(zone: Zone) extends Actor {
  private val doorCloser = context.actorOf(
    Props[DoorCloseActor](), s"${zone.id}-local-door-closer"
  )
  private val hackClearer = context.actorOf(
    Props[HackClearActor](), s"${zone.id}-local-hack-clearer"
  )
  private val hackCapturer = context.actorOf(
    Props[HackCaptureActor](), s"${zone.id}-local-hack-capturer"
  )
  private val captureFlagManager = context.actorOf(
    Props(classOf[CaptureFlagManager], zone), s"${zone.id}-local-capture-flag-manager"
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
        case LocalAction.DeployItem(item) =>
          val definition = item.Definition
          val objectData = definition.Packet.ConstructorData(item).get
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.SendResponse(ObjectCreateMessage(definition.ObjectId, item.GUID, objectData))
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
        case LocalAction.DeployableUIFor(item) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.DeployableUIFor(item)
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
        case LocalAction.EliminateDeployable(obj, guid, pos, effect) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.EliminateDeployable(obj, guid, pos, effect)
            )
          )
        case LocalAction.HackClear(player_guid, target, unk1, unk2) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.SendHackMessageHackCleared(target.GUID, unk1, unk2)
            )
          )
        case LocalAction.HackTemporarily(player_guid, _, target, hackValue, hackClear, duration, unk2) =>
          hackClearer ! HackClearActor.ObjectIsHacked(target, zone, hackClear, unk2, duration)
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/Local", player_guid, LocalResponse.HackObject(target.GUID, hackValue, unk2))
          )
        case LocalAction.ClearTemporaryHack(_, target) =>
          hackClearer ! HackClearActor.ObjectIsResecured(target)

        case LocalAction.ResecureCaptureTerminal(target, hacker) =>
          hackCapturer ! HackCaptureActor.ResecureCaptureTerminal(target, zone, hacker)
        case LocalAction.StartCaptureTerminalHack(target) =>
          hackCapturer ! HackCaptureActor.StartCaptureTerminalHack(target, zone, 0, 8L)
        case LocalAction.LluCaptured(llu) =>
          hackCapturer ! HackCaptureActor.FlagCaptured(llu)
        case LocalAction.LluLost(llu) =>
          hackCapturer ! HackCaptureActor.FlagLost(llu)

        case LocalAction.LluSpawned(player_guid, llu) =>
          // Forward to all clients to create object locally
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.LluSpawned(llu)
            )
          )

        case LocalAction.LluDespawned(player_guid, guid, position) =>
          // Forward to all clients to destroy object locally
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.LluDespawned(guid, position)
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
              LocalResponse.PlanetsideAttribute(target_guid, attribute_number, attribute_value)
            )
          )
        case LocalAction.SendGenericObjectActionMessage(player_guid, target_guid, action_number) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.GenericObjectAction(target_guid, action_number)
            )
          )

        case LocalAction.SendChatMsg(player_guid, msg) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.ChatMessage(msg)
            )
          )

        case LocalAction.SendGenericActionMessage(player_guid, action_number) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              player_guid,
              LocalResponse.GenericActionMessage(action_number)
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
        case LocalAction.ForceZoneChange(zone) =>
          LocalEvents.publish(
            LocalServiceResponse(
              s"/$forChannel/Local",
              Service.defaultPlayerGUID,
              LocalResponse.ForceZoneChange(zone)
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
          LocalResponse.ProximityTerminalEffect(terminal.GUID, effectState = true)
        )
      )
    case Terminal.StopProximityEffect(terminal) =>
      LocalEvents.publish(
        LocalServiceResponse(
          s"/${zone.id}/Local",
          PlanetSideGUID(0),
          LocalResponse.ProximityTerminalEffect(terminal.GUID, effectState = false)
        )
      )

    // Forward all CaptureFlagManager messages
    case msg: CaptureFlagManager.Command =>
      captureFlagManager.forward(msg)

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }
}
