// Copyright (c) 2017 PSForever
package services.galaxy

import net.psforever.objects.zones.HotSpotInfo
import net.psforever.packet.game.BuildingInfoUpdateMessage

object GalaxyResponse {
  trait Response

  final case class HotSpotUpdate(zone_id : Int, priority : Int, host_spot_info : List[HotSpotInfo]) extends Response
  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends Response
}
