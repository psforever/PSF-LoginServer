// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorLogging, ActorRef, Identify}
import akka.io._
import scodec.bits.ByteVector
import scodec.interop.akka._

final case class ReceivedPacket(msg : ByteVector, from : InetSocketAddress)
final case class SendPacket(msg : ByteVector, to : InetSocketAddress)
final case class Hello()
final case class HelloFriend(next: ActorRef)

class UdpListener(nextActor: ActorRef, address : InetAddress, port : Int) extends Actor {
  private val logger = org.log4s.getLogger

  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(address, port))

  var bytesRecevied = 0L
  var bytesSent = 0L

  def receive = {
    case Udp.Bound(local) =>
      logger.info(s"Now listening on UDP:$local")

      nextActor ! Hello()
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case SendPacket(msg, to) =>
      bytesSent += msg.size
      socket ! Udp.Send(msg.toByteString, to)
    case Udp.Received(data, remote) =>
      bytesRecevied += data.size
      nextActor ! ReceivedPacket(data.toByteVector, remote)
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
    case default => logger.error(s"Unhandled message: $default")
  }
}