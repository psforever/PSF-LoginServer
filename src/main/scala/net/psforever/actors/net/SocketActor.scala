package net.psforever.actors.net

import java.net.InetSocketAddress
import java.security.SecureRandom
import java.util.UUID.randomUUID
import java.util.concurrent.ThreadLocalRandom

import akka.actor.Cancellable
import akka.{actor => classic}
import akka.actor.typed.{ActorRef, ActorTags, Behavior, PostStop, Terminated}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.io.{IO, Udp}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.packet.PlanetSidePacket
import net.psforever.util.Config
import scodec.interop.akka.EnrichedByteString

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.util.Random

/** SocketActor creates a UDP socket, receives packets and forwards them to MiddlewareActor
  * There is only one SocketActor, but each connected client gets its own MiddlewareActor
  */
object SocketActor {
  def apply(
      address: InetSocketAddress,
      next: (ActorRef[MiddlewareActor.Command], String) => Behavior[PlanetSidePacket]
  ): Behavior[Command] =
    Behaviors.setup(context => new SocketActor(context, address, next).start())

  sealed trait Command

  private final case class UdpCommandMessage(message: Udp.Command)           extends Command
  private final case class UdpEventMessage(message: Udp.Event)               extends Command
  private final case class UdpUnboundMessage(message: Udp.Unbound)           extends Command
  private final case class Bound(socket: classic.ActorRef)                   extends Command
  private final case class StopChild(ref: ActorRef[MiddlewareActor.Command]) extends Command

  // Typed actors cannot access sender but you can only get the socket that way
  private class SenderHack(ref: ActorRef[SocketActor.Command]) extends classic.Actor {
    def receive: Receive = {
      case Udp.Bound(_) =>
        ref ! Bound(sender())
    }
  }

  // TODO? This doesn't quite support all parameters of the old network simulator
  // Need to decide wheter they are necessary or not
  // https://github.com/psforever/PSF-LoginServer/blob/07f447c2344ab55d581317316c41571772ac2242/src/main/scala/net/psforever/login/UdpNetworkSimulator.scala
  private object NetworkSimulator {
    def apply(socketActor: ActorRef[SocketActor.Command]): Behavior[Udp.Message] =
      Behaviors.setup(context => new NetworkSimulator(context, socketActor))
  }

  private class NetworkSimulator(context: ActorContext[Udp.Message], socketActor: ActorRef[SocketActor.Command])
      extends AbstractBehavior[Udp.Message](context) {

    private[this] val log = org.log4s.getLogger

    override def onMessage(message: Udp.Message): Behavior[Udp.Message] = {
      message match {
        case _: Udp.Received | _: Udp.Send =>
          simulate(message)
          Behaviors.same
        case other =>
          socketActor ! toSocket(message)
          Behaviors.same
      }
    }

    def simulate(message: Udp.Message): Unit = {
      if (Random.nextDouble() > Config.app.development.netSim.loss) {
        if (Random.nextDouble() <= Config.app.development.netSim.reorderChance) {
          context.scheduleOnce(
            ThreadLocalRandom.current().nextDouble(0.01, 0.2).seconds,
            socketActor,
            toSocket(message)
          )
        } else {
          socketActor ! toSocket(message)
        }
      } else {
        log.info("Network simulator dropped packet")
      }
    }

    def toSocket(message: Udp.Message): Command =
      message match {
        case message: Udp.Command => UdpCommandMessage(message)
        case message: Udp.Event   => UdpEventMessage(message)
      }
  }
}

class SocketActor(
    context: ActorContext[SocketActor.Command],
    address: InetSocketAddress,
    next: (ActorRef[MiddlewareActor.Command], String) => Behavior[PlanetSidePacket]
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

  val updUnboundAdapter: ActorRef[Udp.Unbound] = context.messageAdapter[Udp.Unbound](UdpUnboundMessage)
  val senderHack: classic.ActorRef             = context.actorOf(classic.Props(new SenderHack(context.self)))

  IO(Udp)(context.system.classicSystem).tell(Udp.Bind(updEventAdapter.toClassic, address), senderHack)

  val random = new SecureRandom()

  val packetActors: mutable.Map[InetSocketAddress, ActorRef[MiddlewareActor.Command]] = mutable.Map()

  val incomingTimes: mutable.Map[InetSocketAddress, Long] = mutable.Map()
  val outgoingTimes: mutable.Map[InetSocketAddress, Long] = mutable.Map()

  val sessionReaper: Cancellable = context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds)(() => {
    val now = System.currentTimeMillis()
    packetActors.keys.foreach(addr => {
      incomingTimes.get(addr) match {
        case Some(time) =>
          if (now - time > Config.app.network.session.inboundGraceTime.toMillis) {
            context.self ! StopChild(packetActors(addr))
          }
        case _ => ()
      }
      outgoingTimes.get(addr) match {
        case Some(time) =>
          if (now - time > Config.app.network.session.outboundGraceTime.toMillis) {
            context.self ! StopChild(packetActors(addr))
          }
        case _ => ()
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
                case Some(ref) => ref
                case None =>
                  val connectionId = randomUUID.toString
                  val ref = context.spawn(
                    MiddlewareActor(udpCommandAdapter, remote, next, connectionId),
                    s"middleware-${connectionId}",
                    ActorTags(s"uuid=${connectionId}")
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
            case None              => log.warn(s"Received Terminated for unknown actor ${ref}")
          }
          Behaviors.same
      }
  }
}
