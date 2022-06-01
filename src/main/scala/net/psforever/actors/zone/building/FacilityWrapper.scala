// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.{actor => classic}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.structures.Building
import net.psforever.services.InterstellarClusterService

/**
  * A package class that conveys the important information for handling facility updates.
  * These sorts of smaller facilities have power systems that are similar to major facilities
  * but they lack the installed components to support such functionality.
  * A free-floating unlimited power source is provided.
  * @see `FacilityLogic`
  * @see `FakeNtuSource`
  * @param building building entity
  * @param context message-passing reference
  * @param galaxyService event system for state updates to the whole server
  * @param interstellarCluster event system for behavior updates from the whole server
  */
final case class FacilityWrapper(
                                  building: Building,
                                  context: ActorContext[BuildingActor.Command],
                                  galaxyService: classic.ActorRef,
                                  interstellarCluster: ActorRef[InterstellarClusterService.Command]
                                )
  extends BuildingWrapper {
  /** a custom source for nanite transfer units */
  val supplier = new FakeNtuSource(building)
}
