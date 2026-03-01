// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Actor
import net.psforever.services.Service
import net.psforever.services.base.bus.GenericEventBus
import net.psforever.services.base.envelope.{GenericMessageEnvelope, GenericResponseEnvelope}
import org.log4s.Logger

trait EventSystemStamp

abstract class GenericEventService(stamp: EventSystemStamp)
  extends Actor {
  protected lazy val log: Logger = org.log4s.getLogger(getClass.getSimpleName)

  protected val eventBus: GenericEventBus = new GenericEventBus

  private def commonJoinBehavior: Receive = {
    case Service.Join(channel, true) =>
      val path = formatChannel(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
      who ! Service.JoinConfirmation(self, channel)

    case Service.Join(channel, _) =>
      val path = formatChannel(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
  }

  private def commonLeaveBehavior: Receive = {
    case Service.Leave(None) =>
      eventBus.unsubscribe(sender())

    case Service.Leave(Some(channel)) =>
      val path = formatChannel(channel)
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

  protected def composeResponseEnvelope(msg: GenericMessageEnvelope): GenericResponseEnvelope = {
    msg.response(stamp, formatChannel)
  }

  protected def formatChannel(channel: String): String = s"/$channel"
}
