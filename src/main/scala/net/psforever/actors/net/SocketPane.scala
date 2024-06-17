// Copyright (c) 2024 PSForever
package net.psforever.actors.net

import net.psforever.util.Config

import java.net.{InetAddress, InetSocketAddress}
//import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.packet.PlanetSidePacket

final case class SocketPanePortRotation(
                                           portNumbers: Seq[Int],
                                           index: Int = 0
                                         ) {
  private var currentIndex: Int = index

  def NextPort: Int = this.synchronized {
    val out = portNumbers.lift(currentIndex).getOrElse(Config.app.world.port)
    currentIndex += 1
    currentIndex %= portNumbers.size
    out
  }
}

object SocketPanePortRotation {
  def apply(rotation: SocketPanePortRotation): SocketPanePortRotation = {
    SocketPanePortRotation(rotation.portNumbers, rotation.currentIndex)
  }

  def apply(rotation: SocketPanePortRotation, newPort: Int): SocketPanePortRotation = {
    SocketPanePortRotation(rotation.portNumbers :+ newPort, rotation.currentIndex)
  }
}

/** SocketActor creates a UDP socket, receives packets and forwards them to MiddlewareActor
 * There is only one SocketActor, but each connected client gets its own MiddlewareActor
 */
object SocketPane {
  def apply(
             address: InetAddress,
             nextPlan: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket]
           ): Behavior[Command] =
    Behaviors.setup(context => new SocketPane(context, address, nextPlan).start())

  sealed trait Command

  final case class CreateNewSocket(port: Int) extends Command

  private var rotation: SocketPanePortRotation = SocketPanePortRotation(Array(Config.app.world.port))

  def Rotation: SocketPanePortRotation = SocketPanePortRotation(rotation)

  final def getDefaultPorts: Seq[Int] = {
    val config = Config.app.world
    (config.port +: config.ports).distinct
  }
}

class SocketPane(
                  context: ActorContext[SocketPane.Command],
                  address: InetAddress,
                  next: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket]
                 ) {
  private[this] val log = org.log4s.getLogger

  private var socketActors: Array[ActorRef[SocketActor.Command]] = SocketPane.getDefaultPorts.map { i =>
    context.spawn(SocketActor(new InetSocketAddress(address, i), next), name=s"world-socket-$i")
  }.toArray
  SocketPane.rotation = SocketPanePortRotation(SocketPane.getDefaultPorts)

  log.info(s"Configured ${SocketPane.getDefaultPorts.size} game world instance ports")

  def start(): Behavior[SocketPane.Command] = {
    Behaviors
      .receiveMessagePartial[SocketPane.Command] {
        case SocketPane.CreateNewSocket(port)
          if SocketPane.Rotation.portNumbers.contains(port) =>
          Behaviors.same

        case SocketPane.CreateNewSocket(port) =>
          socketActors = socketActors :+ context.spawn(SocketActor(new InetSocketAddress(address, port), next), name=s"world-socket-$port")
          SocketPane.rotation = SocketPanePortRotation(SocketPane.rotation, port)
          log.info(s"Requested new socket to port $port has been created")
          Behaviors.same

        case _ =>
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          socketActors.foreach(context.stop)
          SocketPane.rotation = SocketPanePortRotation(Array(Config.app.world.port))
          Behaviors.same
      }
  }
}
