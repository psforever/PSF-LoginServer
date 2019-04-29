// Copyright (c) 2017 PSForever
package services.galaxy

import net.psforever.packet.game.{BuildingInfoUpdateMessage, HotSpotInfo}

object GalaxyAction {
  trait Action

  final case class HotSpotUpdate(zone_id : Int, priority : Int, host_spot_info : List[HotSpotInfo]) extends Action
  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends Action
}
