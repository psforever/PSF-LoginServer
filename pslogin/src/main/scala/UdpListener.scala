// Copyright (c) 2016 PSForever.net to present
import java.net.InetSocketAddress

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.io._
import scodec.bits.ByteVector
import scodec.interop.akka._

final case class ReceivedPacket(msg : ByteVector, from : InetSocketAddress)
final case class SendPacket(msg : ByteVector, to : InetSocketAddress)

class UdpListener(nextActor: ActorRef) extends Actor with ActorLogging {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 51000))

  var bytesRecevied = 0L
  var bytesSent = 0L

  def receive = {
    case Udp.Bound(local) =>
      println("UDP bound: " + local)
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
    case x : Any => log.error("Unhandled message: " + x.toString)
  }
}