// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.transfer

import akka.actor.ActorRef
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.entity.{Identifiable, WorldEntity}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.zones.ZoneAware

trait TransferContainer
  extends PlanetSideGameObject
  with Identifiable
  with ZoneAware
  with WorldEntity
  with FactionAffinity {
  def Actor : ActorRef
}

object TransferContainer {
  /**
    * The kind of resource that gets transferred.
    */
  trait TransferMaterial
}
