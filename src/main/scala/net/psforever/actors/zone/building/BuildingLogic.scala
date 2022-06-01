// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails}
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.types.PlanetSideEmpire
import org.log4s.Logger

/**
  * Logic that dictates what happens to a particular type of building
  * when it receives certain messages on its governing control.
  * Try not to transform this into instance classes.
  */
trait BuildingLogic {
  import BuildingActor.Command

  /**
    * Produce a log that borrows from the building name.
    * @param details package class that conveys the important information
    * @return the custom log
    */
  protected def log(details: BuildingWrapper): Logger = {
    org.log4s.getLogger(details.building.Name)
  }

  /**
    * Update the status of the relationship between a component installed in a facility
    * and the facility's status itself.
    * @param details package class that conveys the important information
    * @param entity the installed `Amenity` entity
    * @param data optional information
    * @return the next behavior for this control agency messaging system
    */
  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command]

  /**
    * The facility has lost power.
    * Update all related subsystems and statuses.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def powerOff(details: BuildingWrapper): Behavior[Command]

  /**
    * The facility has regained power.
    * Update all related subsystems and statuses.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def powerOn(details: BuildingWrapper): Behavior[Command]

  /**
    * The facility has run out of nanite resources.
    * Update all related subsystems and statuses.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def ntuDepleted(details: BuildingWrapper): Behavior[Command]

  /**
    * The facility has had its nanite resources restored, even if partially.
    * Update all related subsystems and statuses.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def suppliedWithNtu(details: BuildingWrapper): Behavior[Command]

  /**
    * The facility will change its faction affiliation.
    * Update all related subsystems and statuses.
    * @param details package class that conveys the important information
    * @param faction the faction affiliation to which the facility will update
    * @return the next behavior for this control agency messaging system
    */
  def setFactionTo(details: BuildingWrapper, faction: PlanetSideEmpire.Value): Behavior[Command]

  /**
    * A facility that influences this facility has changed its faction affiliation.
    * Update all related subsystems and statuses of this facility.
    * @param details package class that conveys the important information
    * @param building the neighbor facility that has had its faction changed
    * @return the next behavior for this control agency messaging system
    */
  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command]

  /**
    * The facility has had its nanite resources changed in some way.
    * Update all related subsystems and statuses of this facility.
    * @see `NtuCommand.Command`
    * @param details package class that conveys the important information
    * @param msg the original message that instigated this upoate
    * @return the next behavior for this control agency messaging system
    */
  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command]

  /**
    * Produce an appropriate representation of the facility for the given logic implementation.
    * @param building building entity
    * @param context message-passing reference
    * @param details temporary storage to retain still-allocating reousces during facility startup
    * @return the representation of the building and assorted connecting and reporting outlets
    */
  def wrapper(
               building: Building,
               context: ActorContext[BuildingActor.Command],
               details: BuildingControlDetails
             ): BuildingWrapper = {
    BasicBuildingWrapper(building, context, details.galaxyService, details.interstellarCluster)
  }
}
