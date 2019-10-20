// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, SquadRequestType, SquadWaypoints, Vector3}

object SquadAction {
  trait Action

  final case class InitSquadList() extends Action
  final case class InitCharId() extends Action

  final case class Definition(guid : PlanetSideGUID, line : Int, action : SquadAction) extends Action
  final case class Membership(request_type : SquadRequestType.Value, unk2 : Long, unk3 : Option[Long], player_name : String, unk5 : Option[Option[String]]) extends Action
  final case class Waypoint(event_type : WaypointEventAction.Value, waypoint_type : SquadWaypoints.Value, unk : Option[Long], waypoint_info : Option[WaypointInfo]) extends Action
  final case class Update(char_id : Long, health : Int, max_health : Int, armor : Int, max_armor : Int, pos : Vector3, zone_number : Int) extends Action
}
