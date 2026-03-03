// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.{ActorContext, ActorRef}
import net.psforever.services.Service
import net.psforever.services.base.envelope.{GenericMessageEnvelope, GenericResponseEnvelope, MessageTransformationBehavior, NoReply, Undelivered}
import net.psforever.services.base.message.{EventMessage, EventResponse, SelfRespondingEvent}
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

/**
 * A message when there should be none, but a message is required to be defined anyway.
 * @see `GenericSupportEnvelopeOnly`
 */
case object NoMessage extends SelfRespondingEvent

/**
 * A response when there should be none, but a response is required to be defined anyway.
 * @see `GenericSupportEnvelopeOnly`
 * @see `NoReply`
 * @see `Undelivered`
 */
case object NoResponseEnvelope extends GenericResponseEnvelope {
  def reply: EventResponse = NoReply
  def stamp: EventSystemStamp = Undelivered
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
}

/**
 * A framework for how support actors are to be submitted to an event system.
 * The bare bones are a label by which the support actor is identified for message routing,
 * and a function that constructs the support actor within the context of the event system.
 * @see `ActorContext`
 */
trait EventServiceSupport {
  def label: String
  def constructor(@unused context: ActorContext): ActorRef
}

/**
 * A framework for communicating messages to support actors within an event system.
 * The bare bones are a label by which the support actor is identified for message routing,
 * and the message payload for the support actor to process.
 * @see `ActorContext`
 */
trait GenericMessageToSupport {
  def supportLabel: String
  def supportMessage: Any
}

/**
 * An envelope framework for communicating messages to support actors within an event system
 * and also interacting with the event system directly.
 */
trait GenericSupportEnvelope
  extends GenericMessageToSupport
    with MessageTransformationBehavior

/**
 * An envelope framework for communicating messages to support actors within an event system only.
 */
trait GenericSupportEnvelopeOnly
  extends GenericMessageToSupport
    with GenericMessageEnvelope {
  def originalChannel: String = ""
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
  def msg: EventMessage = NoMessage

  def response(@unused stamp: EventSystemStamp): GenericResponseEnvelope = NoResponseEnvelope
}

/**
 * Advanced opt-in event response relay system.
 * Includes a system of specialized child actors that serve specific repeatable operations
 * whose behaviors are intentionally synchronized on this event pipeline
 * or whose collective operational overhead can be streamlined by reliance on a singular pipeline.
 * @param stamp distinct tag associated with an event system
 * @param eventSupportServices list of support actors to initialize
 */
abstract class GenericEventServiceWithSupport
(
  stamp: EventSystemStamp,
  eventSupportServices: List[EventServiceSupport]
) extends GenericEventService(stamp) {

  private val supportServices: Map[String, ActorRef] =
    eventSupportServices
      .map { supportService => (supportService.label, supportService.constructor(context)) }
      .toMap[String, ActorRef]

  /**
   * Use the label assigned to a support actor
   * to locate that support actor on this event system
   * and forward to it a payload message.
   * @param msg event system message carrying an additional message for a support actor
   */
  private def forwardToSupport(msg: GenericMessageToSupport): Unit = {
    supportServices
      .get(msg.supportLabel)
      .map { support =>
        support.forward(msg.supportMessage)
        msg
      }
      .getOrElse {
        log.error(s"support service ${msg.supportLabel} was not found - check message routing or service params")
      }
  }

  /**
   * If the message involves the support actor subsystem, forward the message to it for additional processing.
   * Afterwards, or if not, merely call up to previously-established message handling if warranted.
   * @param event event system message that may be carrying an additional message for a support actor
   */
  override protected def handleMessage(event: GenericMessageEnvelope): Unit = {
    event match {
      case msg: GenericSupportEnvelopeOnly =>
        forwardToSupport(msg)
      case msg: GenericSupportEnvelope =>
        forwardToSupport(msg)
        super.handleMessage(event)
      case event =>
        super.handleMessage(event)
    }
  }
}
