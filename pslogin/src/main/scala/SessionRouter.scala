// Copyright (c) 2016 PSForever.net to present
import java.net.InetSocketAddress

import akka.actor._
import org.log4s.MDC
import scodec.bits._

import scala.collection.mutable
import MDCContextAware.Implicits._
import akka.actor.MDCContextAware.MdcMsg
import akka.actor.SupervisorStrategy.Stop

final case class RawPacket(data : ByteVector)
final case class ResponsePacket(data : ByteVector)

case class SessionState(id : Long, address : InetSocketAddress, pipeline : List[ActorRef]) {
  def startOfPipe = pipeline.head
  def nextOfStart = pipeline.tail.head
}

case class SessionPipeline(nameTemplate : String, props : Props)

class SessionRouter(pipeline : List[SessionPipeline]) extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger(self.path.name)

  val idBySocket = mutable.Map[InetSocketAddress, Long]()
  val sessionById = mutable.Map[Long, SessionState]()
  val sessionByActor = mutable.Map[ActorRef, SessionState]()

  var sessionId = 0L // this is a connection session, not an actual logged in session ID
  var inputRef : ActorRef = ActorRef.noSender

  override def supervisorStrategy = OneForOneStrategy() { case _ => Stop }

  override def preStart = {
    log.info("SessionRouter started...ready for PlanetSide sessions")
  }

  /**
    * Login sessions are divided between two actors. The crypto session actor transparently handles all of the cryptographic
    * setup of the connection. Once a correct crypto session has been established, all packets, after being decrypted
    * will be passed on to the login session actor. This actor has important state that is used to maintain the login
    * session.
    *
    *                      > PlanetSide Session Pipeline <
    *
    *            read()                  route                decrypt
    * UDP Socket -----> [Session Router] -----> [Crypto Actor] -----> [Session Actor]
    *      ^              |          ^           |        ^                 |
    *      |     write()  |          |  encrypt  |        |   response      |
    *      +--------------+          +-----------+        +-----------------+
   */

  def receive = initializing

  def initializing : Receive = {
    case Hello() =>
      inputRef = sender()
      context.become(started)
    case default =>
      log.error(s"Unknown message $default. Stopping...")
      context.stop(self)
  }

  def started : Receive = {
    case ReceivedPacket(msg, from) =>
      if(idBySocket.contains(from)) {
        MDC("sessionId") = idBySocket{from}.toString

        log.trace(s"Handling recieved packet")
        sessionById{idBySocket{from}}.startOfPipe !> RawPacket(msg)

        MDC.clear()
      } else {
        val session = createNewSession(from)
        idBySocket{from} = session.id

        sessionById{session.id} = session
        sessionByActor{session.startOfPipe} = session
        sessionByActor{session.nextOfStart} = session

        MDC("sessionId") = session.id.toString

        log.info("New session from " + from.toString)

        // send the initial message with MDC context (give the session ID to the lower layers)
        sessionById{session.id}.startOfPipe !> HelloFriend(sessionById{session.id}.nextOfStart)
        sessionById{session.id}.startOfPipe !> RawPacket(msg)

        MDC.clear()
      }
    case ResponsePacket(msg) =>
      val session = sessionByActor.get(sender())

      // drop any old queued messages from old actors
      //if(session.isDefined) {
        log.trace(s"Sending response ${msg}")

        inputRef ! SendPacket(msg, session.get.address)
      //}
    case Terminated(actor) =>
      val terminatedSession = sessionByActor.get(actor)

      if(terminatedSession.isDefined) {
        removeSessionById(terminatedSession.get.id, s"${actor.path.name} died")
      }
    case default =>
      log.error(s"Unknown message $default")
  }

  def createNewSession(address : InetSocketAddress) = {
    val id = newSessionId



    // inflate the pipeline
    val actors = pipeline.map { actor =>
      val a = context.actorOf(actor.props, actor.nameTemplate + id.toString)
      context.watch(a)
      a
    }

    /*val cryptoSession = context.actorOf(Props[CryptoSessionActor],
      "crypto-session-" + id.toString)
    val loginSession = context.actorOf(Props[LoginSessionActor],
      "login-session-" + id.toString)*/

    //context.watch(cryptoSession)
    //context.watch(loginSession)

    SessionState(id, address, actors)
  }

  def removeSessionById(id : Long, reason : String) : Unit = {
    val sessionOption = sessionById.get(id)

    if(!sessionOption.isDefined)
      return

    val session = sessionOption.get

    // TODO: add some sort of delay to prevent old session packets from coming through
    // kill all session specific actors
    session.pipeline.foreach(_ ! PoisonPill)
    session.pipeline.foreach(sessionByActor remove _)
    sessionById.remove(id)
    idBySocket.remove(session.address)

    log.info(s"Stopping session ${id} (reason: $reason)")
  }

  def newSessionId = {
    val oldId = sessionId
    sessionId += 1
    oldId
  }
}
