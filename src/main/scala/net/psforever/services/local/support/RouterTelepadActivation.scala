// Copyright (c) 2017 PSForever
package net.psforever.services.local.support

import net.psforever.objects.zones.Zone
import net.psforever.objects._

import scala.concurrent.duration._

object RouterTelepadActivation {
  final case class AddTask(obj: PlanetSideGameObject, zone: Zone, duration: Option[FiniteDuration] = None)

  final case class ActivateTeleportSystem(telepad: PlanetSideGameObject, zone: Zone)
}
