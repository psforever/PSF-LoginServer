// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.{actor => classic}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.structures.Building
import net.psforever.services.InterstellarClusterService

final case class BuildingControlDetails(
                                         building: Building,
                                         context: ActorContext[BuildingActor.Command],
                                         galaxyService: classic.ActorRef,
                                         interstellarCluster: ActorRef[InterstellarClusterService.Command]
                                       ) {
  var hasNtuSupply: Boolean = true
}
