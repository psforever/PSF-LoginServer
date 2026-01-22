// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Actor
import net.psforever.services.Service
import net.psforever.types.PlanetSideGUID
import org.log4s.Logger

trait GenericResponseEnvelope
  extends GenericEventBusMsg {
  def exclude: PlanetSideGUID
  def reply: EventResponse
}

trait GenericMessageEnvelope {
  def channel: String
  def exclude: PlanetSideGUID
  def msg: EventMessage
  def response(outChannel: String): GenericResponseEnvelope
}

abstract class GenericEventService[IN <: GenericMessageEnvelope, OUT <: GenericResponseEnvelope](busName: String)
  extends Actor {
  protected lazy val log: Logger = org.log4s.getLogger(getClass.getSimpleName)

  protected val eventBus = new GenericEventBus[OUT]

  def commonJoinBehavior: Receive = {
    case Service.Join(channel) =>
      val path = formatChannelOnBusName(channel, busName)
      val who  = sender()
      eventBus.subscribe(who, path)
  }

  def commonLeaveBehavior: Receive = {
    case Service.Leave(None) =>
      eventBus.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = formatChannelOnBusName(channel, busName)
      eventBus.unsubscribe(sender(), path)

    case Service.LeaveAll() =>
      eventBus.unsubscribe(sender())
  }

  def receive: Receive =
    commonJoinBehavior.orElse(commonLeaveBehavior)
      .orElse {
        case msg: IN =>
          compose(msg)

        case msg => ()
          log.warn(s"Unhandled message $msg from ${sender()}")
      }

  protected def compose(msg: GenericMessageEnvelope): Unit = {
    eventBus.publish(msg.response(formatChannelOnBusName(msg.channel, busName)).asInstanceOf[OUT])
  }

  def formatChannelOnBusName: (String, String) => String = GenericEventService.BusOnChannelFormat
}

object GenericEventService {
  final def BusOnChannelFormat(channel: String, busName: String): String = {
    if (channel.trim.isEmpty) {
      s"/$busName"
    } else {
      s"/$channel/$busName"
    }
  }
}
