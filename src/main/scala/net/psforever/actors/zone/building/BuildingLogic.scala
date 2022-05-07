// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.types.PlanetSideEmpire
import org.log4s.Logger

trait BuildingLogic {
  import BuildingActor.Command

  protected def log(details: BuildingControlDetails): Logger = {
    org.log4s.getLogger(details.building.Name)
  }

  def updateForceDome(details: BuildingControlDetails, stateOpt: Option[Boolean]): Behavior[Command]

  /**
    * Evaluate the conditions of the building
    * and determine if its capitol force dome state should be updated
    * to reflect the actual conditions of the base or its surrounding bases.
    * If this building is considered a subcapitol facility to the zone's actual capitol facility,
    * and has the capitol force dome has a dependency upon it,
    * pass a message onto that facility that it should check its own state alignment.
    * @param mapUpdateOnChange if `true`, dispatch a `MapUpdate` message for this building
    */
  def alignForceDomeStatus(details: BuildingControlDetails, mapUpdateOnChange: Boolean = true): Behavior[Command]

  def amenityStateChange(details: BuildingControlDetails, entity: Amenity, data: Option[Any]): Behavior[Command]

  def powerOff(details: BuildingControlDetails): Behavior[Command]

  def powerOn(details: BuildingControlDetails): Behavior[Command]

  def ntuDepleted(details: BuildingControlDetails): Behavior[Command]

  def suppliedWithNtu(details: BuildingControlDetails): Behavior[Command]

  def setFactionTo(details: BuildingControlDetails, faction: PlanetSideEmpire.Value): Behavior[Command]

  def alertToFactionChange(details: BuildingControlDetails, building: Building): Behavior[Command]

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  def powerLost(details: BuildingControlDetails): Behavior[Command]

  /**
    * Power has been restored.
    * All installed amenities are distributed a `PowerOn` message
    * and are instructed to display their "powered" model.
    * Additionally, the facility is now rendered spawnable if its player spawning amenities are online.
    */
  def powerRestored(details: BuildingControlDetails): Behavior[Command]

  def ntu(details: BuildingControlDetails, msg: NtuCommand.Command): Behavior[Command]
}
