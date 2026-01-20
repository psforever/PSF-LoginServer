// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.{ActorContext, ActorRef}

import scala.annotation.unused

trait EventServiceSupport {
  def label: String
  def constructor(@unused context: ActorContext): ActorRef
}

trait GenericMessageToSupportEnvelope
  extends GenericMessageEnvelope {
  def toSupport: String
  def response(outChannel: String): GenericResponseEnvelope = null
}

abstract class GenericEventServiceWithSupport[OUT <: GenericResponseEnvelope](
                                                                               busName: String,
                                                                               eventSupportServices: List[EventServiceSupport]
                                                                             )
  extends GenericEventService[OUT](busName) {

  private val supportServices: Map[String, ActorRef] =
    eventSupportServices
      .map { supportService => (supportService.label, supportService.constructor(context)) }
      .toMap[String, ActorRef]

  private def supportReceive: Receive = {
    case msg: GenericMessageToSupportEnvelope =>
      forwardToSupport(msg)
  }

  override def receive: Receive = supportReceive.orElse(super.receive)

  private def forwardToSupport(msg: GenericMessageToSupportEnvelope): Unit = {
    supportServices
      .get(msg.toSupport)
      .map { support =>
        support.forward(msg)
        msg
      }
      .getOrElse {
        log.error(s"support service ${msg.toSupport} was not found - check message routing or service params")
      }
    if (msg.response(outChannel = "") != null) {
      compose(msg)
    }
  }
}
