// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.EventMessage
import net.psforever.types.PlanetSideGUID

trait GenericMessageEnvelope
  extends AllEnvelopes {
  def originalChannel: String
  def msg: EventMessage
  def response(stamp: EventSystemStamp, sendToChannel: String => String): GenericResponseEnvelope
}

object GenericMessageEnvelope {
  def unapply(obj: GenericMessageEnvelope): Option[(String, PlanetSideGUID, EventMessage)] = {
    Some((obj.originalChannel, obj.filter, obj.msg))
  }
}
