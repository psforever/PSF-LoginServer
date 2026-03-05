// Copyright (c) 2026 PSForever
package net.psforever.services.base

import akka.actor.Actor
import net.psforever.services.Service
import net.psforever.services.base.bus.GenericEventBus
import net.psforever.services.base.envelope.GenericMessageEnvelope
import org.log4s.Logger

/**
 * A distinct tag associated with an event system.
 * The stamp is intended to demonstrate that the input message has been interpreted into an output response
 * through actual use of an event system
 * and that the response has not been fabricated and is not fraudulent.
 * While the word "stamp" more probably calls to mind the concept of a postage stamp,
 * the purpose of this artifact is closer to that of the routing information
 * stamped onto an envelope over the postage stamp area of a letter.
 */
trait EventSystemStamp {
  /*
  Example:
  The channels are "foo", "foo.fizz", and "foo.buzz"
  In general, Classifier channels will perform left-pattern matching
  Publishing to channel "foo" will allocate Classifiers "foo", "foo.fizz", and "foo.buzz"
  To isolate "foo", one must distinguish it with a right-pattern such as "out"
  Publishing to channel "foo.out" no longer publishes to "foo.fizz.out" or to "foo.buzz.out"
  */
  /**
   * Take an input channel and produce the publishing output channel.
   * @param channel publishing channel
   * @return appended publishing channel
   */
  def routing(channel: String): String = s"/$channel/out"
}

/**
 * Basic opt-in event response relay system.
 * Remembers "subscribers" (`ActorRef`) to "channels" (`String`);
 * accepts "messages" (`GenericMessageEnvelope`) and interprets the message as a response (`GenericResponseEnvelope`);
 * and, dispatches the response to all subscribers associated with the channel provided in the message.
 * @param stamp distinct tag associated with an event system
 */
class GenericEventService(stamp: EventSystemStamp)
  extends Actor {
  protected lazy val log: Logger = org.log4s.getLogger(getClass.getSimpleName)

  protected val eventBus: GenericEventBus = new GenericEventBus

  /**
   * Add subscription handling.
   */
  private def commonJoinBehavior: Receive = {
    case Service.Join(channel, true) =>
      val path = stamp.routing(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
      who ! Service.JoinConfirmation(self, channel)

    case Service.Join(channel, _) =>
      val path = stamp.routing(channel)
      val who  = sender()
      eventBus.subscribe(who, path)
  }

  /**
   * Remove subscription handling.
   */
  private def commonLeaveBehavior: Receive = {
    case Service.LeaveAll =>
      eventBus.unsubscribe(sender())

    case Service.Leave(channel) =>
      val path = stamp.routing(channel)
      eventBus.unsubscribe(sender(), path)
  }

  /**
   * Accept and handle designated messages.
   */
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

  /**
   * Handle designated messages.
   * Interpret the input message as an output response and publish that response.
   * @param event event system message
   */
  protected def handleMessage(event: GenericMessageEnvelope): Unit = {
    eventBus.publish(event.response(stamp))
  }
}
