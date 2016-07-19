// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, Terminated}
import akka.io._
import scodec.bits._
import scodec.interop.akka._
import akka.util.ByteString

final case class ReceivedPacket(msg : ByteVector, from : InetSocketAddress)
final case class SendPacket(msg : ByteVector, to : InetSocketAddress)
final case class Hello()
final case class HelloFriend(next: ActorRef)

class UdpListener(nextActorProps : Props, nextActorName : String, address : InetAddress, port : Int) extends Actor {
  private val log = org.log4s.getLogger(self.path.name)

  override def supervisorStrategy = OneForOneStrategy() {
    case _ => Stop
  }

  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(address, port))

  var bytesRecevied = 0L
  var bytesSent = 0L
  var nextActor : ActorRef = Actor.noSender

  def receive = {
    case Udp.Bound(local) =>
      log.info(s"Now listening on UDP:$local")

      createNextActor()

      context.become(ready(sender()))
    case default =>
      log.error(s"Unexpected message $default")
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
    case Terminated(actor) =>
      log.error(s"Next actor ${actor.path.name} has died...restarting")
      createNextActor()
    case default => log.error(s"Unhandled message: $default")
  }

  def createNextActor() = {
    nextActor = context.actorOf(nextActorProps, nextActorName)
    context.watch(nextActor)
    nextActor ! Hello()
  }
}