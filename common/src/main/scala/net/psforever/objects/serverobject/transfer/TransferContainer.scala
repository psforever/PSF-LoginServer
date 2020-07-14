// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.transfer

import akka.actor.ActorRef
import net.psforever.objects.entity.{Identifiable, WorldEntity}
import net.psforever.objects.zones.ZoneAware

trait TransferContainer extends Identifiable
  with ZoneAware
  with WorldEntity {
  def Actor : ActorRef
}

object TransferContainer {
  /**
    * The kind of resource that gets transferred.
    */
  trait TransferMaterial
}
