// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.{ActorContext, ActorRef}
import net.psforever.services.Service
import net.psforever.services.base.envelope.{GenericMessageEnvelope, GenericResponseEnvelope, MessageTransformationBehavior, NoReply, Undelivered}
import net.psforever.services.base.message.{EventMessage, EventResponse, SelfRespondingEvent}
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

case object NoMessage extends SelfRespondingEvent

case object NoResponseEnvelope extends GenericResponseEnvelope {
  def reply: EventResponse = NoReply
  def stamp: EventSystemStamp = Undelivered
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
}

trait EventServiceSupport {
  def label: String
  def constructor(@unused context: ActorContext): ActorRef
}

trait GenericMessageToSupport {
  def supportLabel: String
  def supportMessage: Any
}

trait GenericSupportEnvelope
  extends GenericMessageToSupport
    with MessageTransformationBehavior

trait GenericSupportEnvelopeOnly
  extends GenericMessageToSupport
    with GenericMessageEnvelope {
  def originalChannel: String = ""
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
  def msg: EventMessage = NoMessage

  def response(@unused stamp: EventSystemStamp, @unused sendToChannel: String => String): GenericResponseEnvelope = NoResponseEnvelope
}

abstract class GenericEventServiceWithSupport
(
  stamp: EventSystemStamp,
  eventSupportServices: List[EventServiceSupport]
) extends GenericEventService(stamp) {

  private val supportServices: Map[String, ActorRef] =
    eventSupportServices
      .map { supportService => (supportService.label, supportService.constructor(context)) }
      .toMap[String, ActorRef]

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

  override protected def handleMessage(event: GenericMessageEnvelope): Unit = {
    event match {
      case msg: GenericSupportEnvelopeOnly =>
        forwardToSupport(msg)
      case msg: GenericSupportEnvelope =>
        forwardToSupport(msg)
        eventBus.publish(composeResponseEnvelope(event))
      case event =>
        eventBus.publish(composeResponseEnvelope(event))
    }
  }
}
