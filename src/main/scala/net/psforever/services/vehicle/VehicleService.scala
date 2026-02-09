// Copyright (c) 2017 PSForever
package net.psforever.services.vehicle

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.zones.Zone
import net.psforever.services.base.{EventServiceSupport, GenericEventServiceWithCacheAndSupport, GenericMessageEnvelope}
import net.psforever.services.vehicle.support.TurretUpgrader

case object TurretUpgradeSupport
  extends EventServiceSupport {
  def label: String = "turretUpgrader"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[TurretUpgrader](), name = "TurretUpgrader")
  }
}

class VehicleService(zone: Zone)
  extends GenericEventServiceWithCacheAndSupport[VehicleServiceResponse](
    busName = "Vehicle",
    eventSupportServices = List(TurretUpgradeSupport)
  ) {
  protected def composeResponseEnvelope(msg: GenericMessageEnvelope): VehicleServiceResponse = {
    VehicleServiceResponse(formatChannelOnBusName(msg.channel), msg.filter, msg.msg.response())
  }
}
