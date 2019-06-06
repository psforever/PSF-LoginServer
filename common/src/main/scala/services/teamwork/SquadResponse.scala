// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.packet.game.{PlanetSideGUID, PlanetSideZoneID, SquadInfo, SquadPositionDetail}

object SquadResponse {
  trait Response

  final case class Init(info : Vector[SquadInfo]) extends Response
  final case class Update(infos : Iterable[(Int, SquadInfo)]) extends Response
  final case class Remove(infos : Iterable[Int]) extends Response

  final case class Detail(guid : PlanetSideGUID, leader : String, task : String, zone : PlanetSideZoneID, member_info : List[SquadPositionDetail]) extends Response
}
