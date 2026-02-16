// Copyright (c) 2017-2026 PSForever
package net.psforever.services.vehicle

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.services.base.{EventServiceSupport, EventSystemStamp, GenericEventServiceWithCacheAndSupport}
import net.psforever.services.vehicle.support.TurretUpgrader

case object TurretUpgradeSupport
  extends EventServiceSupport {
  def label: String = "turretUpgrader"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[TurretUpgrader](), name = "TurretUpgrader")
  }
}

case object VehicleStamp extends EventSystemStamp

class VehicleService
  extends GenericEventServiceWithCacheAndSupport(
    stamp = VehicleStamp,
    eventSupportServices = List(TurretUpgradeSupport)
  )
