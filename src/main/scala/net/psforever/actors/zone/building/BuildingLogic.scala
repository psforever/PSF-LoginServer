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
