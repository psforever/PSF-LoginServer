// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse}
import net.psforever.types.PlanetSideGUID

/**
 * A response when there should be none, but a reply is required to be defined anyway.
 */
case object NoReply extends EventResponse

/**
 * A stamp that represents not having been processed by an event system.
 * Should never been given out to an event system.
 */
case object Undelivered extends EventSystemStamp

/**
 * The mechanics of a proper event system envelope.
 * The envelope has two forms: the input message envelope and the output response envelope.
 * The event system uses its stamp to mark that the message form converts into the response form when being processed.
 * In terms of proper metaphor,
 * it makes no sense for a post office take a letter out of its original envelope,
 * write a completely different envelope,
 * write a completely different letter,
 * package the new letter in the new envelope,
 * and deliver it is place of the originals.
 * That would be considered fraud.
 */
trait MessageTransformationBehavior
  extends GenericMessageEnvelope
    with GenericResponseEnvelope {
  private var outputStamp: EventSystemStamp = Undelivered
  private var outputChannel: String = originalChannel
  private var outputReply: EventResponse = NoReply

  // satisfies GenericMessageEnvelope (and GenericResponseEnvelope)
  def channel: String = outputChannel

  // satisfies GenericMessageEnvelope
  def response(stamp: EventSystemStamp): GenericResponseEnvelope = {
    outputStamp = stamp
    outputChannel = stamp.routing(originalChannel)
    outputReply = msg.response()
    this
  }

  // satisfies GenericResponseEnvelope
  def stamp: EventSystemStamp = outputStamp

  def reply: EventResponse = outputReply
}
/**
 * A proper event system envelope.
 */
case class MessageEnvelope(originalChannel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends MessageTransformationBehavior
