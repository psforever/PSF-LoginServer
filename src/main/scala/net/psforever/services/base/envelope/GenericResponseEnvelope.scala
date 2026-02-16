// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse}
import net.psforever.types.PlanetSideGUID

trait GenericResponseEnvelope
  extends AllEnvelopes {
  def reply: EventResponse
  def stamp: EventSystemStamp
}

object GenericResponseEnvelope {
  def apply(stamp: EventSystemStamp, channel: String, filter: PlanetSideGUID, msg: EventMessage): GenericResponseEnvelope ={
    val envelope = MessageEnvelope(channel, filter, msg)
    envelope.response(stamp, s => s)
  }

  def unapply(obj: GenericResponseEnvelope): Option[(String, PlanetSideGUID, EventResponse)] =
    Some((obj.channel, obj.filter, obj.reply))
}
