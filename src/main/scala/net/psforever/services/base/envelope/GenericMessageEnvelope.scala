// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.EventMessage
import net.psforever.types.PlanetSideGUID

trait GenericMessageEnvelope
  extends AllEnvelopes {
  /** input payload transported by this envelope */
  def msg: EventMessage
  /** method that counts as "processing" the envelope by an event system;
   * the event system supplies their stamp and converts the message envelope into a response envelope;
   * the input message is converted into a response message */
  def response(stamp: EventSystemStamp): GenericResponseEnvelope
}

object GenericMessageEnvelope {
  /**
   * The `unapply`ed data from a message envelope resembles the data from includes the filter and the channel information.
   * The original channel information.
   * @param obj response envelope
   * @return a tuple containing the channel, filter, and reply message
   */
  def unapply(obj: GenericMessageEnvelope): Option[(String, PlanetSideGUID, EventMessage)] = {
    Some((obj.channel, obj.filter, obj.msg))
  }
}
