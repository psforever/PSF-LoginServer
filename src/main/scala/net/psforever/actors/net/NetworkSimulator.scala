// Copyright (c) 2024 PSForever
package net.psforever.actors.net

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.io.Udp
import net.psforever.util.Config

import java.util.concurrent.ThreadLocalRandom
import scala.util.Random
import scala.concurrent.duration._

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
      case _ =>
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
      log.trace("Network simulator dropped packet")
    }
  }

  def toSocket(message: Udp.Message): SocketActor.Command =
    message match {
      case message: Udp.Command => SocketActor.UdpCommandMessage(message)
      case message: Udp.Event   => SocketActor.UdpEventMessage(message)
    }
}
