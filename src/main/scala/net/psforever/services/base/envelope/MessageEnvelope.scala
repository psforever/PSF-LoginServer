// Copyright (c) 2026 PSForever
package net.psforever.services.base.envelope

import net.psforever.services.base.EventSystemStamp
import net.psforever.services.base.message.{EventMessage, EventResponse}
import net.psforever.types.PlanetSideGUID

case object NoReply extends EventResponse

case object Undelivered extends EventSystemStamp

trait MessageTransformationBehavior
  extends GenericMessageEnvelope
    with GenericResponseEnvelope {
  private var outputStamp: EventSystemStamp = Undelivered
  private var outputChannel: String = originalChannel
  private var outputReply: EventResponse = NoReply

  // satisfies GenericMessageEnvelope2 (and GenericResponseEnvelope2)
  def channel: String = outputChannel

  // satisfies GenericMessageEnvelope2
  def response(stamp: EventSystemStamp, sendToChannel: String => String): GenericResponseEnvelope = {
    outputStamp = stamp
    outputChannel = sendToChannel(originalChannel)
    outputReply = msg.response()
    this
  }

  // satisfies GenericResponseEnvelope2
  def stamp: EventSystemStamp = outputStamp

  def reply: EventResponse = outputReply
}

case class MessageEnvelope(originalChannel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends MessageTransformationBehavior