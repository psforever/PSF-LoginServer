// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails}
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.types.PlanetSideEmpire
import org.log4s.Logger

trait BuildingLogic {
  import BuildingActor.Command

  protected def log(details: BuildingWrapper): Logger = {
    org.log4s.getLogger(details.building.Name)
  }

  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command]

  def powerOff(details: BuildingWrapper): Behavior[Command]

  def powerOn(details: BuildingWrapper): Behavior[Command]

  def ntuDepleted(details: BuildingWrapper): Behavior[Command]

  def suppliedWithNtu(details: BuildingWrapper): Behavior[Command]

  def setFactionTo(details: BuildingWrapper, faction: PlanetSideEmpire.Value): Behavior[Command]

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command]

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  def powerLost(details: BuildingWrapper): Behavior[Command]

  /**
    * Power has been restored.
    * All installed amenities are distributed a `PowerOn` message
    * and are instructed to display their "powered" model.
    * Additionally, the facility is now rendered spawnable if its player spawning amenities are online.
    */
  def powerRestored(details: BuildingWrapper): Behavior[Command]

  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command]

  def wrapper(
               building: Building,
               context: ActorContext[BuildingActor.Command],
               details: BuildingControlDetails
             ): BuildingWrapper = {
    BasicBuildingWrapper(building, context, details.galaxyService, details.interstellarCluster)
  }
}
