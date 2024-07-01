// Copyright (c) 2024 PSForever
package net.psforever.actors.net

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}

import java.net.{InetAddress, InetSocketAddress}
import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.packet.PlanetSidePacket

private[net] object SocketPanePortRotation {
  /**
   * Overrode constructor for `SocketPanePortRotation` entities.
   * Copy constructor, essentially, that retains the internal current rotation index.
   * @param rotation the previous rotation entity
   * @return a copy of the previous rotation entity
   */
  def apply(rotation: SocketPanePortRotation): SocketPanePortRotation = {
    SocketPanePortRotation(rotation.portNumbers, rotation.currentIndex)
  }

  /**
   * Overrode constructor for `SocketPanePortRotation` entities.
   * Adda new port to the list of ports but retain the internal current rotation index.
   * @param rotation the previous rotation entity
   * @param newPort the new port number
   * @return a copy of the previous rotation entity with an additional port that can be selected
   */
  def apply(rotation: SocketPanePortRotation, newPort: Int): SocketPanePortRotation = {
    SocketPanePortRotation(rotation.portNumbers :+ newPort, rotation.currentIndex)
  }
}

/**
 * For a given sequence of ports,
 * cycle through port numbers until the last port number has been produced
 * then start from the first port number again.
 * @param portNumbers the sequence of ports to be cycled between
 * @param index optional;
 *              the starting index in the sequence of ports;
 *              default is 0
 */
private[net] case class SocketPanePortRotation(
                                                portNumbers: Seq[Int],
                                                index: Int = 0
                                              ) {
  private var currentIndex: Int = index

  /**
   * Retrieve the sequentially next port number.
   * @return the next port number
   */
  def NextPort: Int = {
    val out = portNumbers.lift(currentIndex).orElse(portNumbers.headOption).getOrElse(0)
    currentIndex += 1
    currentIndex %= portNumbers.size
    out
  }
}

/**
 * Information explaining how to manage a socket group.
 * @param groupId moniker for the group
 * @param info how the sockets in this group operate
 * @param initial should this socket group be protected as "original";
 *                defaults to `false`
 */
final case class SocketSetup(groupId: String, info: SocketSetupInfo, initial: Boolean = false) {
  assert(info.ports.nonEmpty, s"port group $groupId should define port numbers")
  /* the same port can belong to multiple groups, but the same port may not be repeated in the same group */
  assert(info.ports.size == info.ports.distinct.size, s"port group $groupId should not contain duplicate port numbers")
}

/**
 * Information explaining the details of a socket group.
 * @param address Internet protocol location of the host
 * @param ports network ports that are used as endpoints of transmission;
 *              corresponds to a number of sockets
 * @param planOfAction whenever a new connection across a socket is made, the method of consuming network packets
 */
final case class SocketSetupInfo(
                                  address: InetAddress,
                                  ports: Seq[Int],
                                  planOfAction: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket]
                                )

object SocketPane {
  val SocketPaneKey: ServiceKey[Command] = ServiceKey[SocketPane.Command](id = "socketPane")

  /**
   * Overrode constructor for `SocketPane` entities.
   * Registers the entity with the actor receptionist.
   * @param setupInfo the details of the socket groups
   * @return a `SocketPane` entity
   */
  def apply(setupInfo: Seq[SocketSetup]): Behavior[Command] = {
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(SocketPaneKey, context.self)
      new SocketPane(context, setupInfo).start()
    }
  }

  sealed trait Command

  final case class CreateNewSocket(groupId: String, port: Int) extends Command

  final case class CreateNewSocketGroup(groupId: String, info: SocketSetupInfo) extends Command

  final case class GetNextPort(groupId: String, replyTo: classic.ActorRef) extends Command

  final case class NextPort(groupId: String, address: InetAddress, port: Int)
}

/**
 * Management of sockets connecting to the network ports.
 * <br><br>
 * Connections to the networking ports are created by the logic imposed by this class
 * and are handled by sockets that bind to those ports and accept or pass packets to game logic.
 * This game logic is encapsulated by an anonymous function
 * that is automatically implemented into a game logic machine (consumption and production)
 * upon unique connections detected / attempted across those sockets.
 * Multiple sockets can connect to the same port so no compensation is required.
 * <br><br>
 * New sockets to ports can be added to existing groups after the initial socket groups.
 * New socket groups can be created after the initial information.
 * @param context hook for setting up the sockets and, eventually, their packet logic
 * @param initialPortSetup the details of the socket groups
 */
class SocketPane(
                  context: ActorContext[SocketPane.Command],
                  private val initialPortSetup: Seq[SocketSetup]
                 ) {
  private[this] val log = org.log4s.getLogger

  /** original socket group information, now properly flagged as "original" */
  private var socketConfigs: Seq[SocketSetup] = initialPortSetup.map { setup => setup.copy(initial = true) }
  /** all sockets produced by the socket group information and any later socket creation commands */
  private var socketActors: Array[ActorRef[SocketActor.Command]] = initialPortSetup.flatMap {
      case SocketSetup(_, SocketSetupInfo(address, ports, plan), _) =>
        ports.map { portNum => context.spawn(SocketActor(new InetSocketAddress(address, portNum), plan), name=s"world-socket-$portNum") }
    }.toArray
  /** load balancing for redirecting newly discovered packet input to different sockets (ports);
   * should be referenced externally to switch sockets;
   * see SocketActor.GetNextPort */
  private var socketRotations: Array[SocketPanePortRotation] = initialPortSetup.map {
    case SocketSetup(_, SocketSetupInfo(_, ports, _), _) => SocketPanePortRotation(ports)
  }.toArray

  log.debug(s"sockets configured for ${socketActors.length} ports initially")

  def start(): Behavior[SocketPane.Command] = {
    Behaviors
      .receiveMessagePartial[SocketPane.Command] {
        case SocketPane.CreateNewSocket(key, _)
          if !socketConfigs.exists { setup => setup.groupId.equals(key) } =>
          log.warn(s"port group $key does not exist and can not be appended to")
          Behaviors.same

        case SocketPane.CreateNewSocket(groupId, port)
        if socketConfigs
          .find { setup => setup.groupId.equals(groupId) }
          .exists { case SocketSetup(_, SocketSetupInfo(_, ports, _), _) => ports.contains(port) } =>
          log.info(s"new port $port for group $groupId already supported")
        Behaviors.same

        case SocketPane.CreateNewSocket(groupId, port) =>
          log.info(s"new socket to port $port created in $groupId")
          val index = socketConfigs.indexWhere { setup => setup.groupId.equals(groupId) }
          val SocketSetup(_, SocketSetupInfo(address, ports, plan), _) = socketConfigs(index)
          socketActors = socketActors :+
            context.spawn(SocketActor(new InetSocketAddress(address, port), plan), name=s"world-socket-$port")
          socketConfigs = (socketConfigs.take(index) :+ SocketSetup(groupId, SocketSetupInfo(address, ports :+ port, plan))) ++
            socketConfigs.drop(index + 1)
          socketRotations = (socketRotations.take(index) :+ SocketPanePortRotation(socketRotations(index), port)) ++
            socketRotations.drop(index + 1)
          Behaviors.same

        case SocketPane.CreateNewSocketGroup(groupId, _)
          if socketConfigs.exists { case SocketSetup(oldKey, _, _) => oldKey.equals(groupId) } =>
          log.warn(s"port group $groupId already exists and can not be created twice")
          Behaviors.same

        case SocketPane.CreateNewSocketGroup(groupId, info @ SocketSetupInfo(address, ports, plan)) =>
          socketActors = socketActors ++
            ports.map { portNum => context.spawn(SocketActor(new InetSocketAddress(address, portNum), plan), name=s"world-socket-$portNum") }
          socketConfigs = socketConfigs :+
            SocketSetup(groupId, info)
          socketRotations = socketRotations :+
            SocketPanePortRotation(ports)
          Behaviors.same

        case SocketPane.GetNextPort(groupId, replyTo) =>
          socketConfigs.indexWhere { setup => setup.groupId.equals(groupId) } match {
            case -1 =>
              log.warn(s"port group $groupId does not exist")
            case index =>
              replyTo ! SocketPane.NextPort(groupId, socketConfigs(index).info.address, socketRotations(index).NextPort)
          }
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          socketActors.foreach(context.stop)
          Behaviors.same
      }
  }
}
