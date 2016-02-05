import java.net.InetSocketAddress

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import scodec.bits._

import scala.collection.mutable

final case class RawPacket(data : ByteVector)

class SessionRouter extends Actor with ActorLogging {
  val sessions = mutable.Map[InetSocketAddress, ActorRef]()
  var sessionId = 0L

  def receive = {
    case ReceivedPacket(msg, from) =>
      if(sessions.contains(from)) {
        sessions{from} ! RawPacket(msg)
      } else {
        log.info("New session from " + from.toString)

        val id = newSessionId
        val loginSession = new LoginSession(id, sender(), from)
        val ref = context.actorOf(Props(new LoginSessionActor(loginSession)),
          "login-session" + id.toString)

        sessions{from} = ref

        ref ! RawPacket(msg)
      }
    case _ => log.error("Unknown message")
  }

  def newSessionId = {
    val oldId = sessionId
    sessionId += 1
    oldId
  }
}
