// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.llu

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior

/**
  * An `Actor` that handles messages being dispatched to a lattice logic unit (LLU) socket.
  * Actually does nothing lol
  * @param socket the socket entity being governed
  */
class CaptureFlagSocketControl(socket: CaptureFlagSocket)
  extends Actor
  with FactionAffinityBehavior.Check {
  def FactionObject      = socket

  def receive: Receive = {
    case _ => ;
  }
}
