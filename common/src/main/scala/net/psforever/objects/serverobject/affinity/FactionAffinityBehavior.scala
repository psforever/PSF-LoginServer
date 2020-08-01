// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.affinity

import akka.actor.Actor

object FactionAffinityBehavior {

  /**
    * A `trait` for inheritance of common implementable methods.
    */
  sealed trait BasicAffinity {
    def FactionObject: FactionAffinity
  }

  /**
    * The logic governing `FactionAffinity` objects that use the `ConvertFactionAffinity` message.
    * This is a mix-in trait for combining with existing `Receive` logic.
    */
  trait Convert extends BasicAffinity {
    this: Actor =>

    val convertBehavior: Receive = {
      case FactionAffinity.ConvertFactionAffinity(faction) =>
        FactionObject.Faction = faction
        sender() ! FactionAffinity.AssertFactionAffinity(FactionObject, faction)
    }
  }

  /**
    * The logic governing `FactionAffinity` objects that use the `ConfirmFactionAffinity` message.
    * A case exists to catch `AssertFactionAffinity` messages for the same ends though they should not be used this way.
    * This is a mix-in trait for combining with existing `Receive` logic.
    */
  trait Check extends BasicAffinity {
    this: Actor =>

    val checkBehavior: Receive = {
      case FactionAffinity.ConfirmFactionAffinity() | FactionAffinity.AssertFactionAffinity(_, _) =>
        sender() ! FactionAffinity.AssertFactionAffinity(FactionObject, FactionObject.Faction)
    }
  }
}
