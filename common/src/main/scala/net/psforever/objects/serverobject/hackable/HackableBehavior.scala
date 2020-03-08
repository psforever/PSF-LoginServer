// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.hackable

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages

object HackableBehavior {
  /**
    * The logic governing generic `Hackable` objects that use the `Hack` and `ClearHack` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    * @see `Hackable`
    */
  trait GenericHackable {
    this : Actor =>
    def HackableObject : Hackable

    val hackableBehavior : Receive = {
      case CommonMessages.Hack(player, _, _) =>
        val obj = HackableObject
        obj.HackedBy = player
        sender ! true

      case CommonMessages.ClearHack() =>
        val obj = HackableObject
        obj.HackedBy = None
    }
  }
}
