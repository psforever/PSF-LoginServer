// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `Door`.
  * @param door the `Door` object being governed
  */
class DoorControl(door: Door) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject: FactionAffinity = door

  def receive: Receive =
    checkBehavior.orElse {
      case Door.Use(player, msg) =>
        sender() ! Door.DoorMessage(player, msg, door.Use(player, msg))

      case _ =>
        sender() ! Door.NoEvent()
    }
}
