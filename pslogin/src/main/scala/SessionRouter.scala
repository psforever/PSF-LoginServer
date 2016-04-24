// Copyright (c) 2016 PSForever.net to present
import java.net.InetSocketAddress

import akka.actor._
import scodec.bits._

import scala.collection.mutable

final case class RawPacket(data : ByteVector)
final case class ResponsePacket(data : ByteVector)

case class SessionState(id : Long, address : InetSocketAddress, pipeline : List[ActorRef]) {
  def inject(pkt : RawPacket) = pipeline.head ! pkt
}

class SessionRouter extends Actor with ActorLogging {
  val idBySocket = mutable.Map[InetSocketAddress, Long]()
  val sessionById = mutable.Map[Long, SessionState]()
  val sessionByActor = mutable.Map[ActorRef, SessionState]()

  var sessionId = 0L // this is a connection session, not an actual logged in session ID
  var inputRef : ActorRef = ActorRef.noSender

  /*
    Login sessions are divided between two actors. the crypto session actor transparently handles all of the cryptographic
    setup of the connection. Once a correct crypto session has been established, all packets, after being decrypted
    will be passed on to the login session actor. This actor has important state that is used to maintain the login
    session.

                         > PlanetSide Session Pipeline <

               read()                  route                decrypt
    UDP Socket -----> [Session Router] -----> [Crypto Actor] -----> [Session Actor]
         ^              |          ^           |        ^                 |
         |     write()  |          |  encrypt  |        |   response      |
         +--------------+          +-----------+        +-----------------+
   */

  def receive = initializing

  def initializing : Receive = {
    case Hello() =>
      inputRef = sender()
      context.become(started)
    case _ =>
      log.error("Unknown message")
      context.stop(self)
  }

  def started : Receive = {
    case ReceivedPacket(msg, from) =>
      if(idBySocket.contains(from)) {
        sessionById{idBySocket{from}}.inject(RawPacket(msg))
      } else {
        log.info("New session from " + from.toString)

        val session = createNewSession(from)
        idBySocket{from} = session.id

        sessionById{session.id} = session
        sessionByActor{session.pipeline.head} = session

        sessionById{session.id}.inject(RawPacket(msg))
      }
    case ResponsePacket(msg) =>
      val session = sessionByActor{sender()}

      inputRef ! SendPacket(msg, session.address)
    case _ => log.error("Unknown message")
  }

  def createNewSession(address : InetSocketAddress) = {
    val id = newSessionId

    val cryptoSession = context.actorOf(Props[CryptoSessionActor],
      "crypto-session" + id.toString)
    val loginSession = context.actorOf(Props[LoginSessionActor],
      "login-session" + id.toString)

    // start the pipeline setup
    cryptoSession ! HelloFriend(loginSession)

    SessionState(id, address, List(cryptoSession, loginSession))
  }

  def newSessionId = {
    val oldId = sessionId
    sessionId += 1
    oldId
  }
}
