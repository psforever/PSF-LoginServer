// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.teamwork.{Member, Squad}
import net.psforever.packet.game._
import net.psforever.types.SquadRequestType

object SquadResponse {
  trait Response

  final case class Init(info : Vector[SquadInfo]) extends Response
  final case class Update(infos : Iterable[(Int, SquadInfo)]) extends Response
  final case class Remove(infos : Iterable[Int]) extends Response

  final case class Membership(request_type : SquadRequestType.Value, unk1 : Int, unk2 : Int, unk3 : Long, unk4 : Option[Long], player_name : String, unk5 : Boolean, unk6 : Option[Option[String]]) extends Response //see SquadMembershipResponse
  final case class Join(squad : Squad, positionsToUpdate : List[Int]) extends Response
  final case class Leave(squad : Squad, positionsToUpdate : List[(Long, Int)]) extends Response
  final case class UpdateMembers(squad : Squad, update_info : List[SquadAction.Update]) extends Response

  final case class Detail(guid : PlanetSideGUID, leader : String, task : String, zone : PlanetSideZoneID, member_info : List[SquadPositionDetail]) extends Response
}
