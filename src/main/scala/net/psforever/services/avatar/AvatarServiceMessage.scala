// Copyright (c) 2017-2026 PSForever
package net.psforever.services.avatar

import net.psforever.services.Service
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.EventMessage
import net.psforever.types.PlanetSideGUID

object AvatarServiceMessage {
  def apply(channel: String, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, Service.defaultPlayerGUID, msg)

  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, filter, msg)
}
