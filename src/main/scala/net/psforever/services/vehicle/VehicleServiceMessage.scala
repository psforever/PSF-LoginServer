// Copyright (c) 2017 PSForever
package net.psforever.services.vehicle

import net.psforever.services.Service
import net.psforever.services.base.message.EventMessage
import net.psforever.services.base.GenericSupportEnvelopeOnly
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.types.PlanetSideGUID

object VehicleServiceMessage {
  def apply(channel: String, localMessage: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, Service.defaultPlayerGUID, localMessage)

  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, filter, msg)
}

final case class TurretMessage(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "turretUpgrader"
}
