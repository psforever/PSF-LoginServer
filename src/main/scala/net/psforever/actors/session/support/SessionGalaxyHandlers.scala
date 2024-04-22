// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
//
import net.psforever.actors.session.AvatarActor
import net.psforever.packet.game.{BroadcastWarpgateUpdateMessage, HotSpotInfo => PacketHotSpotInfo, HotSpotUpdateMessage, ZoneInfoMessage, ZonePopulationUpdateMessage}
import net.psforever.services.galaxy.GalaxyResponse
import net.psforever.types.{MemberAction, PlanetSideEmpire}

class SessionGalaxyHandlers(
                             val sessionData: SessionData,
                             avatarActor: typed.ActorRef[AvatarActor.Command],
                             galaxyService: ActorRef,
                             implicit val context: ActorContext
                           ) extends CommonSessionInterfacingFunctionality {
  def handle(reply: GalaxyResponse.Response): Unit = {
    reply match {
      case GalaxyResponse.HotSpotUpdate(zone_index, priority, hot_spot_info) =>
        sendResponse(
          HotSpotUpdateMessage(
            zone_index,
            priority,
            hot_spot_info.map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
          )
        )

      case GalaxyResponse.MapUpdate(msg) =>
        sendResponse(msg)

      case GalaxyResponse.UpdateBroadcastPrivileges(zoneId, gateMapId, fromFactions, toFactions) =>
        val faction = player.Faction
        val from = fromFactions.contains(faction)
        val to = toFactions.contains(faction)
        if (from && !to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, PlanetSideEmpire.NEUTRAL))
        } else if (!from && to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, faction))
        }

      case GalaxyResponse.FlagMapUpdate(msg) =>
        sendResponse(msg)

      case GalaxyResponse.TransferPassenger(temp_channel, vehicle, _, manifest) =>
        sessionData.zoning.handleTransferPassenger(temp_channel, vehicle, manifest)

      case GalaxyResponse.LockedZoneUpdate(zone, time) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=false, lock_time=time))

      case GalaxyResponse.UnlockedZoneUpdate(zone) => ;
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=true, lock_time=0L))
        val popBO = 0
        val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
        val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
        val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)
        sendResponse(ZonePopulationUpdateMessage(zone.Number, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))

      case GalaxyResponse.LogStatusChange(name) if (avatar.people.friend.exists { _.name.equals(name) })  =>
        avatarActor ! AvatarActor.MemberListRequest(MemberAction.UpdateFriend, name)

      case GalaxyResponse.SendResponse(msg) =>
        sendResponse(msg)

      case _ => ()
    }
  }
}

/*package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import scala.concurrent.duration._
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.Vehicle
import net.psforever.packet.game.{AvatarDeadStateMessage, BroadcastWarpgateUpdateMessage, DeadState, HotSpotInfo => PacketHotSpotInfo, HotSpotUpdateMessage, ZoneInfoMessage, ZonePopulationUpdateMessage}
import net.psforever.services.Service
import net.psforever.services.galaxy.GalaxyResponse
import net.psforever.types.{MemberAction, PlanetSideEmpire}

class SessionGalaxyHandlers(
                             val sessionData: SessionData,
                             avatarActor: typed.ActorRef[AvatarActor.Command],
                             galaxyService: ActorRef,
                             implicit val context: ActorContext
                           ) extends CommonSessionInterfacingFunctionality {
  def handle(reply: GalaxyResponse.Response): Unit = {
    reply match {
      case GalaxyResponse.HotSpotUpdate(zoneIndex, priority, hotSpotInfo) =>
        sendResponse(
          HotSpotUpdateMessage(
            zoneIndex,
            priority,
            hotSpotInfo.map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
          )
        )

      case GalaxyResponse.MapUpdate(msg) =>
        sendResponse(msg)

      case GalaxyResponse.UpdateBroadcastPrivileges(zoneId, gateMapId, fromFactions, toFactions) =>
        val faction = player.Faction
        val from = fromFactions.contains(faction)
        val to = toFactions.contains(faction)
        if (from && !to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, PlanetSideEmpire.NEUTRAL))
        } else if (!from && to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, faction))
        }

      case GalaxyResponse.FlagMapUpdate(msg) =>
        sendResponse(msg)

      case GalaxyResponse.TransferPassenger(tempChannel, vehicle, _, manifest) =>
        val playerName = player.Name
        log.debug(s"TransferPassenger: $playerName received the summons to transfer to ${vehicle.Zone.id} ...")
        manifest.passengers
          .find { _.name.equals(playerName) }
          .collect {
            case entry if vehicle.Seats(entry.mount).occupant.isEmpty =>
              player.VehicleSeated = None
              vehicle.Seats(entry.mount).mount(player)
              player.VehicleSeated = vehicle.GUID
              Some(vehicle)
            case entry if vehicle.Seats(entry.mount).occupant.contains(player) =>
              Some(vehicle)
            case entry =>
              log.warn(
                s"TransferPassenger: $playerName tried to mount seat ${entry.mount} during summoning, but it was already occupied, and ${player.Sex.pronounSubject} was rebuked"
              )
              None
          }.orElse {
            manifest.cargo.find { _.name.equals(playerName) }.flatMap { entry =>
              vehicle.CargoHolds(entry.mount).occupant.collect {
                case cargo if cargo.Seats(0).occupants.exists(_.Name.equals(playerName)) => cargo
              }
            }
        } match {
          case Some(v: Vehicle) =>
            galaxyService ! Service.Leave(Some(tempChannel)) //temporary vehicle-specific channel (see above)
            sessionData.zoning.spawn.deadState = DeadState.Release
            sendResponse(AvatarDeadStateMessage(DeadState.Release, 0, 0, player.Position, player.Faction, unk5=true))
            sessionData.zoning.interstellarFerry = Some(v) //on the other continent and registered to that continent's GUID system
            sessionData.zoning.spawn.LoadZonePhysicalSpawnPoint(v.Continent, v.Position, v.Orientation, 1 seconds, None)
          case _ =>
            sessionData.zoning.interstellarFerry match {
              case None =>
                galaxyService ! Service.Leave(Some(tempChannel)) //no longer being transferred between zones
                sessionData.zoning.interstellarFerryTopLevelGUID = None
              case Some(_) => ;
              //wait patiently
            }
        }

      case GalaxyResponse.LockedZoneUpdate(zone, time) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=false, lock_time=time))

      case GalaxyResponse.UnlockedZoneUpdate(zone) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=true, lock_time=0L))
        val popBO = 0
        val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
        val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
        val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)
        sendResponse(ZonePopulationUpdateMessage(zone.Number, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))

      case GalaxyResponse.LogStatusChange(name) if avatar.people.friend.exists { _.name.equals(name) } =>
        avatarActor ! AvatarActor.MemberListRequest(MemberAction.UpdateFriend, name)

      case GalaxyResponse.SendResponse(msg) =>
        sendResponse(msg)

      case _ => ()
    }
  }
}*/
