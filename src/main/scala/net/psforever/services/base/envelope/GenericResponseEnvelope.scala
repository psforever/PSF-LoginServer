// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse, SelfRespondingEvent}
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
  /** channel information tailored to the event system */
  final def outChannel: String = stamp.routing(channel)
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
   * Fake a response envelope, as if processed by a specific event system.
   * @param _stamp marker indicating the routing through which the original message was processed
   * @param _channel set of subscribers on an event system bus the envelope should reach
   * @param _filter a specific subscriber endpoint to be excluded
   * @param _reply input payload transported by this envelope
   * @return a faked but typically acceptable response envelope
   */
  def apply(_stamp: EventSystemStamp, _channel: String, _filter: PlanetSideGUID, _reply: SelfRespondingEvent): GenericResponseEnvelope = {
    apply(_stamp, _channel, _filter, _reply.asInstanceOf[EventResponse])
  }

  /**
   * Fake a response envelope, as if processed by a specific event system.
   * @param _stamp marker indicating the routing through which the original message was processed
   * @param _channel set of subscribers on an event system bus the envelope should reach
   * @param _filter a specific subscriber endpoint to be excluded
   * @param _reply input payload transported by this envelope
   * @return a faked but typically acceptable response envelope
   */
  def apply(_stamp: EventSystemStamp, _channel: String, _filter: PlanetSideGUID, _reply: EventResponse): GenericResponseEnvelope = {
    new GenericResponseEnvelope {
      def channel: String = _channel
      def filter: PlanetSideGUID = _filter
      def reply: EventResponse = _reply
      def stamp: EventSystemStamp = _stamp
    }
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
