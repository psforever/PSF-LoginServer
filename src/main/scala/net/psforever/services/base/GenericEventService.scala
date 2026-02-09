// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Actor
import net.psforever.services.Service
import net.psforever.services.base.bus.{AllGenericBusMsg, GenericEventBus, GenericEventBusResponse}
import org.log4s.Logger

import scala.annotation.unused

trait GenericResponseEnvelope
  extends GenericEventBusResponse {
  def reply: EventResponse
}

trait GenericMessageEnvelope
  extends AllGenericBusMsg {
  def msg: EventMessage
}

abstract class GenericEventService[OUT <: GenericResponseEnvelope](busName: String)
  extends Actor {
  protected lazy val log: Logger = org.log4s.getLogger(getClass.getSimpleName)

  protected val eventBus: GenericEventBus[OUT] = new GenericEventBus[OUT]

  def BusName: String = busName

  private def commonJoinBehavior: Receive = {
    case Service.Join(channel) =>
      val path = formatChannelOnBusName(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
  }

  private def commonLeaveBehavior: Receive = {
    case Service.Leave(None) =>
      eventBus.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = formatChannelOnBusName(channel)
      eventBus.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      eventBus.unsubscribe(sender())
  }

  protected def commonBehavior: Receive = {
    case msg: GenericMessageEnvelope =>
      handleMessage(msg)
  }

  def receive: Receive = commonJoinBehavior
    .orElse(commonLeaveBehavior)
    .orElse(commonBehavior)
    .orElse {
      case msg =>
        log.warn(s"Unhandled message $msg from ${sender()}")
    }

  protected def handleMessage(msg: GenericMessageEnvelope): Unit = {
    eventBus.publish(composeResponseEnvelope(msg))
  }

  protected def composeResponseEnvelope(@unused msg: GenericMessageEnvelope): OUT

  protected def formatChannelOnBusName(channel: String): String = s"/$channel/$busName"
}
