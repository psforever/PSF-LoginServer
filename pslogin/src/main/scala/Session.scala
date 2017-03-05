// Copyright (c) 2017 PSForever

import java.net.InetSocketAddress

import akka.actor._
import org.log4s.MDC
import scodec.bits._

import akka.actor.{ActorContext, ActorRef, PoisonPill}
import com.github.nscala_time.time.Imports._
import MDCContextAware.Implicits._

sealed trait SessionState
final case class New() extends SessionState
final case class Related() extends SessionState
final case class Handshaking() extends SessionState
final case class Established() extends SessionState
final case class Closing() extends SessionState
final case class Closed() extends SessionState


class Session(val sessionId : Long,
              val socketAddress : InetSocketAddress,
              returnActor : ActorRef,
              sessionPipeline : List[SessionPipeline])
             (implicit val context: ActorContext, implicit val self : ActorRef)  {

  var state : SessionState = New()
  val sessionCreatedTime : DateTime = DateTime.now
  var sessionEndedTime : DateTime = DateTime.now

  val pipeline = sessionPipeline.map { actor =>
    val a = context.actorOf(actor.props, actor.nameTemplate + sessionId.toString)
    context.watch(a)
    a
  }

  pipeline.head ! HelloFriend(sessionId, pipeline.tail.head)

  // statistics
  var bytesSent : Long = 0
  var bytesReceived : Long = 0
  var inboundPackets : Long = 0
  var outboundPackets : Long = 0

  var lastInboundEvent : Long = System.nanoTime()
  var lastOutboundEvent : Long = System.nanoTime()

  var inboundPacketRate : Double = 0.0
  var outboundPacketRate : Double = 0.0
  var inboundBytesPerSecond : Double = 0.0
  var outboundBytesPerSecond : Double = 0.0

  def receive(packet : RawPacket) : Unit = {
    bytesReceived += packet.data.size
    inboundPackets += 1
    lastInboundEvent = System.nanoTime()

    pipeline.head !> packet
  }

  def send(packet : ByteVector) : Unit = {
    bytesSent += packet.size
    outboundPackets += 1
    lastOutboundEvent = System.nanoTime()

    returnActor ! SendPacket(packet, socketAddress)
  }

  def dropSession(graceful : Boolean) = {
    pipeline.foreach(context.unwatch)
    pipeline.foreach(_ ! PoisonPill)

    sessionEndedTime = DateTime.now
    setState(Closed())
  }

  def getState = state

  def setState(newState : SessionState) : Unit = {
    state = newState
  }
  def getPipeline : List[ActorRef] = pipeline

  def getTotalBytes = {
    bytesSent + bytesReceived
  }

  def timeSinceLastInboundEvent = {
    (System.nanoTime() - lastInboundEvent)/1000000
  }

  def timeSinceLastOutboundEvent = {
    (System.nanoTime() - lastOutboundEvent)/1000000
  }


  override def toString : String = {
    s"Session($sessionId, $getTotalBytes)"
  }
}
