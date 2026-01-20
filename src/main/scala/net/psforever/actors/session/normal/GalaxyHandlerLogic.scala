// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{GalaxyHandlerFunctions, SessionData, SessionGalaxyHandlers}
import net.psforever.packet.game.{BroadcastWarpgateUpdateMessage, FriendsResponse, HotSpotUpdateMessage, ZoneInfoMessage, ZonePopulationUpdateMessage, HotSpotInfo => PacketHotSpotInfo}
import net.psforever.services.base.EventResponse
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.types.{MemberAction, PlanetSideEmpire}

object GalaxyHandlerLogic {
  def apply(ops: SessionGalaxyHandlers): GalaxyHandlerLogic = {
    new GalaxyHandlerLogic(ops, ops.context)
  }
}

class GalaxyHandlerLogic(val ops: SessionGalaxyHandlers, implicit val context: ActorContext) extends GalaxyHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

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

  def handle(reply: EventResponse): Unit = {
    reply match {
      case GalaxyAction.HotSpotUpdate(zone_index, priority, hot_spot_info) =>
        sendResponse(
          HotSpotUpdateMessage(
            zone_index,
            priority,
            hot_spot_info.map { spot => PacketHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
          )
        )

      case GalaxyAction.MapUpdate(msg) =>
        sendResponse(msg)
        import net.psforever.actors.zone.ZoneActor
        import net.psforever.zones.Zones
        Zones.zones.find(_.Number == msg.continent_id) match {
          case Some(zone) =>
            zone.actor ! ZoneActor.BuildingInfoState(msg)
          case None =>
        }

      case GalaxyAction.UpdateBroadcastPrivileges(zoneId, gateMapId, fromFactions, toFactions) =>
        val faction = player.Faction
        val from = fromFactions.contains(faction)
        val to = toFactions.contains(faction)
        if (from && !to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, PlanetSideEmpire.NEUTRAL))
        } else if (!from && to) {
          sendResponse(BroadcastWarpgateUpdateMessage(zoneId, gateMapId, faction))
        }

      case GalaxyAction.FlagMapUpdate(msg) =>
        sendResponse(msg)

      case GalaxyAction.TransferPassenger(_, temp_channel, vehicle, _, manifest) =>
        sessionLogic.zoning.handleTransferPassenger(temp_channel, vehicle, manifest)

      case GalaxyAction.LockedZoneUpdate(zone, time) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=false, lock_time=time))

      case GalaxyAction.UnlockedZoneUpdate(zone) =>
        sendResponse(ZoneInfoMessage(zone.Number, empire_status=true, lock_time=0L))
        val popBO = 0
        val pop = zone.LivePlayers.distinctBy(_.CharId)
        val popTR = pop.count(_.Faction == PlanetSideEmpire.TR)
        val popNC = pop.count(_.Faction == PlanetSideEmpire.NC)
        val popVS = pop.count(_.Faction == PlanetSideEmpire.VS)
        sendResponse(ZonePopulationUpdateMessage(zone.Number, 414, 138, popTR, 138, popNC, 138, popVS, 138, popBO))

      case GalaxyAction.LogStatusChange(name) if avatar.people.friend.exists(_.name.equals(name)) =>
        avatarActor ! AvatarActor.MemberListRequest(MemberAction.UpdateFriend, name)

      case GalaxyAction.SendResponse(msg) =>
        sendResponse(msg)

      case _ => ()
    }
  }
}
