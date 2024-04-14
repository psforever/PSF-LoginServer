// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GalaxyHandlerFunctions, SessionGalaxyHandlers, SessionData}
import net.psforever.packet.game.{BroadcastWarpgateUpdateMessage, FriendsResponse, HotSpotUpdateMessage, ZoneInfoMessage, ZonePopulationUpdateMessage, HotSpotInfo => PacketHotSpotInfo}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage}
import net.psforever.types.{MemberAction, PlanetSideEmpire}

class GalaxyHandlerLogic(val ops: SessionGalaxyHandlers) extends GalaxyHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  implicit val context: ActorContext = ops.context

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  private val galaxyService: ActorRef = ops.galaxyService

  /* packets */

  def handleUpdateIgnoredPlayers(pkt: FriendsResponse): Unit = {
    sendResponse(pkt)
    pkt.friends.foreach { f =>
      galaxyService ! GalaxyServiceMessage(GalaxyAction.LogStatusChange(f.name))
    }
  }

  /* response handlers */

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
        sessionLogic.zoning.handleTransferPassenger(temp_channel, vehicle, manifest)

      case GalaxyResponse.LockedZoneUpdate(zone, time) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=false, lock_time=time))

      case GalaxyResponse.UnlockedZoneUpdate(zone) => ;
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=true, lock_time=0L))
        val popBO = 0
        val popTR = zone.Players.count(_.faction == PlanetSideEmpire.TR)
        val popNC = zone.Players.count(_.faction == PlanetSideEmpire.NC)
        val popVS = zone.Players.count(_.faction == PlanetSideEmpire.VS)
        sendResponse(ZonePopulationUpdateMessage(zone.Number, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))

      case GalaxyResponse.LogStatusChange(name) if avatar.people.friend.exists(_.name.equals(name)) =>
        avatarActor ! AvatarActor.MemberListRequest(MemberAction.UpdateFriend, name)

      case GalaxyResponse.SendResponse(msg) =>
        sendResponse(msg)

      case _ => ()
    }
  }
}
