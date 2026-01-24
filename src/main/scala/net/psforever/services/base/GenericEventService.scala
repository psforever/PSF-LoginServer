// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Actor
import net.psforever.services.Service
import net.psforever.types.PlanetSideGUID
import org.log4s.Logger

import scala.annotation.unused

trait GenericResponseEnvelope
  extends GenericEventBusMsg {
  def filter: PlanetSideGUID
  def reply: EventResponse
}

trait GenericMessageEnvelope {
  def channel: String
  def filter: PlanetSideGUID
  def msg: EventMessage
}

abstract class GenericEventService[OUT <: GenericResponseEnvelope](busName: String)
  extends Actor {
  protected lazy val log: Logger = org.log4s.getLogger(getClass.getSimpleName)

  protected val eventBus = new GenericEventBus[OUT]

  def BusName: String = busName

  def commonJoinBehavior: Receive = {
    case Service.Join(channel) =>
      val path = formatChannelOnBusName(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
  }

  def commonLeaveBehavior: Receive = {
    case Service.Leave(None) =>
      eventBus.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = formatChannelOnBusName(channel)
      eventBus.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      eventBus.unsubscribe(sender())
  }

  def receive: Receive =
    commonJoinBehavior.orElse(commonLeaveBehavior)
      .orElse {
        case msg: GenericMessageEnvelope =>
          handleMessage(msg)

        case msg => ()
          log.warn(s"Unhandled message $msg from ${sender()}")
      }

  protected def handleMessage(msg: GenericMessageEnvelope): Unit = {
    eventBus.publish(compose(msg))
  }

  protected def compose(@unused msg: GenericMessageEnvelope): OUT

  def formatChannelOnBusName(channel: String): String = GenericEventService.BusOnChannelFormat(busName)(channel)
}

object GenericEventService {
  final def BusOnChannelFormat(busName: String)(channel: String): String = {
    if (channel.trim.isEmpty) {
      s"/$busName"
    } else {
      s"/$channel/$busName"
    }
  }
}
