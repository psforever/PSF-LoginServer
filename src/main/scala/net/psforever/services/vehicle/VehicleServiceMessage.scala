// Copyright (c) 2017 PSForever
package net.psforever.services.vehicle

import net.psforever.services.Service
import net.psforever.services.base.{EventMessage, GenericMessageEnvelope, GenericMessageToSupportEnvelopeOnly}
import net.psforever.types.PlanetSideGUID

final case class VehicleServiceMessage(channel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends GenericMessageEnvelope

final case class TurretMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "turretUpgrader"
}

object VehicleServiceMessage {
  def apply(channel: String, actionMessage: EventMessage): VehicleServiceMessage =
    VehicleServiceMessage(channel, Service.defaultPlayerGUID, actionMessage)
}
