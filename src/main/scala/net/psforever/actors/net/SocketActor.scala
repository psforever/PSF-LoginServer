package net.psforever.actors.net

import java.net.InetSocketAddress
import java.security.SecureRandom
import java.util.UUID.randomUUID
import akka.actor.Cancellable
import akka.{actor => classic}
import akka.actor.typed.{ActorRef, ActorTags, Behavior, PostStop, Terminated}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.io.{IO, Udp}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.packet.PlanetSidePacket
import net.psforever.util.Config
import scodec.interop.akka.EnrichedByteString

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

/**
 * Create a networking port attachment that accepts user datagram protocol (UDP) packets
 * and forwards those packets to a business logic unit referred to herein as a "plan".
 * The landing site of a plan is a processing entity of middleware logic that dissects composed packet data
 * and pushes that further down the chain to the business logic.
 * Each instance of middleware support and then business logic
 * is associated with a unique clients that attempt to connect to the server though this socket port.
 */
object SocketActor {
  def apply(
             address: InetSocketAddress,
             nextPlan: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket]
           ): Behavior[Command] =
    Behaviors.setup(context => new SocketActor(context, address, nextPlan).start())

  sealed trait Command

  private[net] final case class UdpCommandMessage(message: Udp.Command)           extends Command
  private[net] final case class UdpEventMessage(message: Udp.Event)               extends Command
  private final case class UdpUnboundMessage(message: Udp.Unbound)           extends Command
  private final case class Bound(socket: classic.ActorRef)                   extends Command
  private final case class StopChild(ref: ActorRef[MiddlewareActor.Command]) extends Command
  final case class AskSocketLoad(replyTo: ActorRef[Any]) extends Command
  final case class SocketLoad(sessions: Int)

  // Typed actors cannot access sender but you can only get the socket that way
  private class SenderHack(ref: ActorRef[SocketActor.Command]) extends classic.Actor {
    def receive: Receive = {
      case Udp.Bound(_) =>
        ref ! Bound(sender())

      case Udp.CommandFailed(_:Udp.Bind) =>
        context.system.terminate()
    }
  }
}

class SocketActor(
                   context: ActorContext[SocketActor.Command],
                   address: InetSocketAddress,
                   nextPlan: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket]
                 ) {
  import SocketActor._
  import SocketActor.Command

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log = org.log4s.getLogger

  val udpCommandAdapter: ActorRef[Udp.Command] =
    if (!Config.app.development.netSim.enable) {
      context.messageAdapter[Udp.Command](UdpCommandMessage)
    } else {
      context.spawnAnonymous(NetworkSimulator(context.self))
    }

  val updEventAdapter: ActorRef[Udp.Event] =
    if (!Config.app.development.netSim.enable) {
      context.messageAdapter[Udp.Event](UdpEventMessage)
    } else {
      context.spawnAnonymous(NetworkSimulator(context.self))
    }

  //val updUnboundAdapter: ActorRef[Udp.Unbound] = context.messageAdapter[Udp.Unbound](UdpUnboundMessage)
  val senderHack: classic.ActorRef = context.actorOf(classic.Props(new SenderHack(context.self)))

  IO(Udp)(context.system.classicSystem).tell(Udp.Bind(updEventAdapter.toClassic, address), senderHack)

  val random = new SecureRandom()

  val packetActors: mutable.Map[InetSocketAddress, ActorRef[MiddlewareActor.Command]] = mutable.Map()

  val incomingTimes: mutable.Map[InetSocketAddress, Long] = mutable.Map()
  val outgoingTimes: mutable.Map[InetSocketAddress, Long] = mutable.Map()

  val sessionReaper: Cancellable = context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds)(() => {
    val now = System.currentTimeMillis()
    packetActors.keys.foreach(addr => {
      packetActors.get(addr) match {
        case Some(child) =>
          if(
            (incomingTimes.get(addr) match {
              case Some(time) =>  now - time > Config.app.network.session.inboundGraceTime.toMillis
              case _          => false
            }) ||
            (outgoingTimes.get(addr) match {
              case Some(time) =>  now - time > Config.app.network.session.outboundGraceTime.toMillis
            case _            => false
          })
          ) {
            context.self ! StopChild(child)
          }
        case _ => ;
      }
    })
  })

  def start(): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case Bound(socket) =>
          active(socket)
      }
      .receiveSignal {
        case (_, PostStop) =>
          sessionReaper.cancel()
          Behaviors.same
      }
  }

  def active(socket: classic.ActorRef): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case UdpEventMessage(message) =>
          message match {
            case Udp.Bound(_) => Behaviors.same
            case Udp.Received(data, remote) =>
              incomingTimes(remote) = System.currentTimeMillis()
              val ref = packetActors.get(remote) match {
                case Some(_ref) => _ref
                case None =>
                  val connectionId = randomUUID.toString
                  val ref = context.spawn(
                    MiddlewareActor(udpCommandAdapter, remote, nextPlan, connectionId),
                    s"middleware-$connectionId",
                    ActorTags(s"uuid=$connectionId")
                  )
                  context.watch(ref)
                  packetActors(remote) = ref
                  ref
              }
              ref ! MiddlewareActor.Receive(data.toByteVector)
              Behaviors.same

            case _ =>
              Behaviors.same
          }
          Behaviors.same

        case UdpCommandMessage(message) =>
          message match {
            case Udp.Send(_, remote, _) =>
              outgoingTimes(remote) = System.currentTimeMillis()
            case _ => ()
          }
          socket ! message
          Behaviors.same

        case AskSocketLoad(replyTo) =>
          replyTo ! SocketLoad(packetActors.size)
          Behaviors.same

        case UdpUnboundMessage(_) =>
          Behaviors.stopped

        case StopChild(ref) =>
          context.stop(ref)
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          sessionReaper.cancel()
          Behaviors.same
        case (_, Terminated(ref)) =>
          packetActors.find(_._2 == ref) match {
            case Some((remote, _)) => packetActors.remove(remote)
            case None              => log.warn(s"Received Terminated for unknown actor $ref")
          }
          Behaviors.same
      }
  }
}
