// Copyright (c) 2019 PSForever
package net.psforever.services.teamwork

import net.psforever.objects.teamwork.Squad
import net.psforever.packet.game.{SquadDetail, SquadInfo, WaypointEventAction, WaypointInfo}
import net.psforever.types.{PlanetSideGUID, SquadResponseType, SquadWaypoints}
import net.psforever.services.GenericEventBusMsg

final case class SquadServiceResponse(channel: String, exclude: Iterable[Long], response: SquadResponse.Response)
    extends GenericEventBusMsg

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

  final case class AssociateWithSquad(squad_guid: PlanetSideGUID) extends Response
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
  final case class WantsSquadPosition(leader_char_id: Long, bid_name: String)                 extends Response
  final case class Join(squad: Squad, positionsToUpdate: List[Int], channel: String)          extends Response
  final case class Leave(squad: Squad, positionsToUpdate: List[(Long, Int)])                  extends Response
  final case class UpdateMembers(squad: Squad, update_info: List[SquadAction.Update])         extends Response
  final case class AssignMember(squad: Squad, from_index: Int, to_index: Int)                 extends Response
  final case class PromoteMember(squad: Squad, char_id: Long, from_index: Int, to_index: Int) extends Response

  final case class Detail(guid: PlanetSideGUID, squad_detail: SquadDetail) extends Response

  final case class InitWaypoints(char_id: Long, waypoints: Iterable[(SquadWaypoints.Value, WaypointInfo, Int)])
      extends Response
  final case class WaypointEvent(
      event_type: WaypointEventAction.Value,
      char_id: Long,
      waypoint_type: SquadWaypoints.Value,
      unk5: Option[Long],
      waypoint_info: Option[WaypointInfo],
      unk: Int
  ) extends Response

  final case class SquadSearchResults() extends Response
}
