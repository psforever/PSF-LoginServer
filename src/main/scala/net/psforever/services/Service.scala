// Copyright (c) 2017 PSForever
package net.psforever.services

import net.psforever.types.PlanetSideGUID

object Service {
  final val defaultPlayerGUID: PlanetSideGUID = PlanetSideGUID(0)

  final case class Startup()

  final case class Join(channel: String)
  final case class Leave(channel: Option[String] = None)
  final case class LeaveAll()
}
