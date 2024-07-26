// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.hackable

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.serverobject.CommonMessages

import scala.annotation.unused

object HackableBehavior {

  /**
   * The logic governing generic `Hackable` objects that use the `Hack` and `ClearHack` message.
   * This is a mix-in trait for combining with existing `Receive` logic.
   * @see `Hackable`
   */
  trait GenericHackable {
    this: Actor =>
    def HackableObject: PlanetSideGameObject with Hackable

    val clearHackBehavior: Receive = {
      case CommonMessages.ClearHack() =>
        performClearHack(None, sender())
    }

    val hackableBehavior: Receive = clearHackBehavior
      .orElse {
        case CommonMessages.Hack(player, _, _) =>
          performHack(player, None, sender())
      }

    def performHack(player: Player, @unused data: Option[Any], replyTo: ActorRef): Unit = {
      val obj = HackableObject
      obj.HackedBy = player
      replyTo ! CommonMessages.EntityHackState(obj, hackState = true)
    }

    def performClearHack(@unused data: Option[Any], replyTo: ActorRef): Unit = {
      val obj = HackableObject
      obj.HackedBy = None
      replyTo ! CommonMessages.EntityHackState(obj, hackState = false)
    }
  }
}
