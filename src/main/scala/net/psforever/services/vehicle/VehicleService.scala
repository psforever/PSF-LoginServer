// Copyright (c) 2017-2026 PSForever
package net.psforever.services.vehicle

import akka.actor.Props
import net.psforever.services.base.{EventSystemStamp, GenericEventServiceWithCacheAndSupport}
import net.psforever.services.vehicle.support.TurretUpgradeSupport

case object VehicleStamp extends EventSystemStamp

object VehicleService {
  def apply(): Props = {
    Props(
      classOf[GenericEventServiceWithCacheAndSupport],
      VehicleStamp,
      List(TurretUpgradeSupport)
    )
  }
}
