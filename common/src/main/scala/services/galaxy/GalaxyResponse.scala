// Copyright (c) 2017 PSForever
package services.galaxy

import net.psforever.packet.game.BuildingInfoUpdateMessage

object GalaxyResponse {
  trait Response

  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends Response
}
