// Copyright (c) 2019 PSForever
package net.psforever.services.teamwork

import akka.actor.ActorRef
import net.psforever.objects.avatar.Certification
import net.psforever.objects.teamwork.Squad
import net.psforever.packet.game.{SquadDetail, SquadInfo, WaypointEventAction, WaypointInfo}
import net.psforever.types.{ChatMessageType, PlanetSideGUID, SquadResponseType, SquadWaypoint}
import net.psforever.services.base.{EventResponse, GenericEventBusMsg}

final case class SquadServiceResponse(channel: String, exclude: Iterable[Long], response: SquadResponse.Response)
     extends EventResponse with GenericEventBusMsg

object SquadServiceResponse {
  def apply(toChannel: String, response: SquadResponse.Response): SquadServiceResponse =
    SquadServiceResponse(toChannel, Nil, response)

  def apply(toChannel: String, exclude: Long, response: SquadResponse.Response): SquadServiceResponse =
    SquadServiceResponse(toChannel, Seq(exclude), response)
}

object SquadResponse {
  sealed trait Response

  final case class ListSquadFavorite(line: Int, task: String) extends Response

  final case class InitList(info: Vector[SquadInfo])             extends Response
  final case class UpdateList(infos: Iterable[(Int, SquadInfo)]) extends Response
  final case class RemoveFromList(infos: Iterable[Int])          extends Response

  final case class IdentifyAsSquadLeader(squad_guid: PlanetSideGUID) extends Response
  final case class SetListSquad(squad_guid: PlanetSideGUID)       extends Response

  final case class Membership(
      request_type: SquadResponseType.Value,
      unk1: Int,
      unk2: Int,
      unk3: Long,
      unk4: Option[Long],
      player_name: String,
      unk5: Boolean,
      unk6: Option[Option[String]]
  )                                                                                           extends Response //see SquadMembershipResponse
  object Membership {
    def apply(
               requestType: SquadResponseType.Value,
               unk3: Long,
               unk4: Option[Long],
               playerName: String,
               unk5: Boolean
             ): Membership = new Membership(requestType, unk1 = 0, unk2 = 0, unk3, unk4, playerName, unk5, Some(None))
  }

  final case class WantsSquadPosition(leader_char_id: Long, bid_name: String)                 extends Response
  final case class Join(squad: Squad, positionsToUpdate: List[Int], channel: String, ref: ActorRef) extends Response
  final case class Leave(squad: Squad, positionsToUpdate: List[(Long, Int)])                  extends Response
  final case class UpdateMembers(squad: Squad, update_info: List[SquadAction.Update])         extends Response
  final case class AssignMember(squad: Squad, from_index: Int, to_index: Int)                 extends Response
  final case class PromoteMember(squad: Squad, char_id: Long, from_index: Int)                extends Response

  final case class Detail(guid: PlanetSideGUID, squad_detail: SquadDetail) extends Response

  final case class InitWaypoints(char_id: Long, waypoints: Iterable[(SquadWaypoint, WaypointInfo, Int)])
      extends Response
  final case class WaypointEvent(
      event_type: WaypointEventAction.Value,
      char_id: Long,
      waypoint_type: SquadWaypoint,
      unk5: Option[Long],
      waypoint_info: Option[WaypointInfo],
      unk: Int
  ) extends Response

  final case class SquadDecoration(guid: PlanetSideGUID, squad: Squad) extends Response

  final case class SquadSearchResults(results: List[PlanetSideGUID]) extends Response

  final case class CharacterKnowledge(
                                       id: Long,
                                       name: String,
                                       certs: Set[Certification],
                                       unk1: Int,
                                       unk2: Int,
                                       zoneNumber: Int
                                     ) extends Response

  final case class SquadRelatedComment(str: String, messageType: ChatMessageType = ChatMessageType.UNK_227) extends Response
}
