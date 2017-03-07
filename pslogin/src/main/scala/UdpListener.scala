// Copyright (c) 2017 PSForever
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, Terminated}
import akka.io._
import scodec.bits._
import scodec.interop.akka._
import akka.util.ByteString

final case class ReceivedPacket(msg : ByteVector, from : InetSocketAddress)
final case class SendPacket(msg : ByteVector, to : InetSocketAddress)
final case class Hello()
final case class HelloFriend(sessionId : Long, next: ActorRef)

class UdpListener(nextActorProps : Props,
                  nextActorName : String,
                  listenAddress : InetAddress,
                  port : Int,
                  netParams : Option[NetworkSimulatorParameters]) extends Actor {
  private val log = org.log4s.getLogger(self.path.name)

  override def supervisorStrategy = OneForOneStrategy() {
    case _ => Stop
  }

  import context.system

  // If we have network parameters, start the network simulator
  if(netParams.isDefined) {
    // See http://www.cakesolutions.net/teamblogs/understanding-akkas-recommended-practice-for-actor-creation-in-scala
    // For why we cant do Props(new Actor) here
    val sim = context.actorOf(Props(classOf[UdpNetworkSimulator], self, netParams.get))
    IO(Udp).tell(Udp.Bind(sim, new InetSocketAddress(listenAddress, port)), sim)
  } else {
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress(listenAddress, port))
  }

  var bytesRecevied = 0L
  var bytesSent = 0L
  var nextActor : ActorRef = Actor.noSender

  def receive = {
    case Udp.Bound(local) =>
      log.info(s"Now listening on UDP:$local")

      createNextActor()
      context.become(ready(sender()))
    case Udp.CommandFailed(Udp.Bind(_, address, _)) =>
      log.error("Failed to bind to the network interface: " + address)
      context.system.terminate()
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