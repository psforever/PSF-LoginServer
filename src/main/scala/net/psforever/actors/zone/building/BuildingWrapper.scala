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
  * @see `BuildingActor`
  * @see `BuildingLogic`
  * @see `BuildingWrapper`
  * @see `GalaxyService`
  * @see `InterstellarClusterService`
  */
trait BuildingWrapper {
  /** building entity */
  def building: Building
  /** message-passing reference */
  def context: ActorContext[BuildingActor.Command]
  /** event system for state updates to the whole server */
  def galaxyService: classic.ActorRef
  /** event system for behavior updates from the whole server */
  def interstellarCluster: ActorRef[InterstellarClusterService.Command]
}
