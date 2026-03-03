// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse}
import net.psforever.types.PlanetSideGUID

/**
 * The framework of an event system envelope that is treated as the processed complement of a message envelope.
 */
trait GenericResponseEnvelope
  extends AllEnvelopes {
  /** result of converting the message envelope input payload into output payload */
  def reply: EventResponse
  /** marker indicating the routing through which the original message was processed */
  def stamp: EventSystemStamp
}

object GenericResponseEnvelope {
  /**
   * Fake a response envelope, as if processed by a specific event system.
   * @param stamp marker indicating the routing through which the original message was processed
   * @param channel set of subscribers on an event system bus the envelope should reach
   * @param filter a specific subscriber endpoint to be excluded
   * @param msg input payload transported by this envelope
   * @return a faked but typically acceptable response envelope
   */
  def apply(stamp: EventSystemStamp, channel: String, filter: PlanetSideGUID, msg: EventMessage): GenericResponseEnvelope = {
    val envelope = MessageEnvelope(channel, filter, msg)
    envelope.response(stamp)
  }

  /**
   * The `unapply`ed data from a response envelope resembles the data from includes the filter and the channel information.
   * @param obj response envelope
   * @return a tuple containing the channel, filter, and reply message
   */
  def unapply(obj: GenericResponseEnvelope): Option[(String, PlanetSideGUID, EventResponse)] = {
    Some((obj.channel, obj.filter, obj.reply))
  }
}
