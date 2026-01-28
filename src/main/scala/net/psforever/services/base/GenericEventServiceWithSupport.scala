// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.{ActorContext, ActorRef}
import net.psforever.services.Service
import net.psforever.services.base.bus.{GenericEventBus, GenericEventBusResponseToSupport, GenericEventBusWithSupport}
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

trait EventServiceSupport {
  def label: String
  def constructor(@unused context: ActorContext): ActorRef
}

trait GenericMessageToSupportEnvelope
  extends GenericMessageEnvelope {
  def supportLabel: String
  def supportMessage: Any
}

trait GenericMessageToSupportEnvelopeOnly
  extends GenericMessageToSupportEnvelope {
  def channel: String = ""
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
  def msg: EventMessage = null
}

abstract class GenericEventServiceWithSupport[OUT <: GenericResponseEnvelope]
(
  busName: String,
  eventSupportServices: List[EventServiceSupport]
) extends GenericEventService[OUT](busName) {

  private val supportServices: Map[String, ActorRef] =
    eventSupportServices
      .map { supportService => (supportService.label, supportService.constructor(context)) }
      .toMap[String, ActorRef]

  private def forwardToSupport(msg: GenericMessageToSupportEnvelope): Unit = {
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

  override protected def setupEventBus(): GenericEventBus[OUT] = {
    new GenericEventBus[OUT] with GenericEventBusWithSupport[OUT] {
      override def publish(event: OUT): Unit = publishingWithSupport(event)

      override def forwardToExternalSupport(msg: GenericEventBusResponseToSupport): Unit = {
        msg match {
          case supportMessage: GenericMessageToSupportEnvelope => forwardToExternal(supportMessage)
          case _ => ()
        }
      }

      private def forwardToExternal(msg: GenericMessageToSupportEnvelope): Unit = {
        forwardToSupport(msg)
      }
    }
  }
}
