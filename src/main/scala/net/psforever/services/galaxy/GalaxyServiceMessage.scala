// Copyright (c) 2017 PSForever
package net.psforever.services.galaxy

import net.psforever.services.Service
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.EventMessage

object GalaxyServiceMessage {
  def apply(msg: EventMessage): MessageEnvelope = MessageEnvelope("", Service.defaultPlayerGUID, msg)

  def apply(channel: String, msg: EventMessage): MessageEnvelope = MessageEnvelope(channel, Service.defaultPlayerGUID, msg)
}
