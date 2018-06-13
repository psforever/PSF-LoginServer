// Copyright (c) 2017 PSForever
package services.galaxy

import net.psforever.packet.game.{BuildingInfoUpdateMessage}

object GalaxyAction {
  trait Action

  final case class MapUpdate(msg: BuildingInfoUpdateMessage) extends Action
}
