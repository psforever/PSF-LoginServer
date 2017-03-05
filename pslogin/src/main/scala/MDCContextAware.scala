// Copyright (c) 2017 PSForever
// Taken from http://code.hootsuite.com/logging-contextual-info-in-an-asynchronous-scala-application/
package akka.actor

import akka.util.Timeout
import org.slf4j.MDC

import scala.concurrent.Future

trait MDCContextAware extends Actor with ActorLogging  {
  import MDCContextAware._

  // This is why this needs to be in package akka.actor
  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    val orig = MDC.getCopyOfContextMap
    try {
      msg match {
        case mdcObj @ MdcMsg(mdc, origMsg) =>
          if (mdc != null)
            MDC.setContextMap(mdc)
          else
            MDC.clear()
          super.aroundReceive(receive, origMsg)
        case _ =>
          super.aroundReceive(receive, msg)
      }
    } finally {
      if (orig != null)
        MDC.setContextMap(orig)
      else
        MDC.clear()
    }
  }
}

object MDCContextAware {
  private case class MdcMsg(mdc: java.util.Map[String, String], msg: Any)

  object Implicits {

    /**
      * Add two new methods that allow MDC info to be passed to MDCContextAware actors.
      *
      * Do NOT use these methods to send to actors that are not MDCContextAware.
      */
    implicit class ContextLocalAwareActorRef(val ref: ActorRef) extends AnyVal {

      import akka.pattern.ask

      /**
        * Send a message to an actor that is MDCContextAware - it will propagate
        * the current MDC values. Note: we MUST capture the ActorContext in order for senders
        * to be correct! This was a bug from the original author.
        */
      def !>(msg: Any)(implicit context: ActorContext) : Unit =
        ref.tell(MdcMsg(MDC.getCopyOfContextMap, msg), context.self)

      /**
        * "Ask" an actor that is MDCContextAware for something - it will propagate
        * the current MDC values
        */
      def ?>(msg: Any)(implicit context: ActorContext, timeout: Timeout): Future[Any] =
        ref.ask(MdcMsg(MDC.getCopyOfContextMap, msg), context.self)
    }
  }
}