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
  * @param building building entity
  * @param context message-passing reference
  * @param galaxyService event system for state updates to the whole server
  * @param interstellarCluster event system for behavior updates from the whole server
  */
final case class BasicBuildingWrapper(
                                       building: Building,
                                       context: ActorContext[BuildingActor.Command],
                                       galaxyService: classic.ActorRef,
                                       interstellarCluster: ActorRef[InterstellarClusterService.Command]
                                     ) extends BuildingWrapper
