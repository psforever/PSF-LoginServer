// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails}
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

case object CavernFacilityLogic
  extends BuildingLogic {
  import BuildingActor.Command

  override def wrapper(
                        building: Building,
                        context: ActorContext[BuildingActor.Command],
                        details: BuildingControlDetails
                      ): BuildingWrapper = {
    FacilityWrapper(building, context, details.galaxyService, details.interstellarCluster)
  }

  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    entity match {
      case terminal: CaptureTerminal =>
        // Notify amenities that listen for CC hack state changes, e.g. wall turrets to dismount seated players
        details.building.Amenities.filter(x => x.isInstanceOf[CaptureTerminalAware]).foreach(amenity => {
          data match {
            case Some(isResecured: Boolean) => amenity.Actor ! CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured)
            case _ => log(details).warn("CaptureTerminal AmenityStateChange was received with no attached data.")
          }
        })
        // When a CC is hacked (or resecured) all currently hacked amenities for the base should return to their default unhacked state
        details.building.HackableAmenities.foreach(amenity => {
          if (amenity.HackedBy.isDefined) {
            details.building.Zone.LocalEvents ! LocalServiceMessage(amenity.Zone.id,LocalAction.ClearTemporaryHack(PlanetSideGUID(0), amenity))
          }
        })
      // No map update needed - will be sent by `HackCaptureActor` when required
      case _ =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(details.building.infoUpdateMessage()))
    }
    Behaviors.same
  }

  def powerOff(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def powerOn(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def ntuDepleted(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def suppliedWithNtu(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def setFactionTo(
                    details: BuildingWrapper,
                    faction: PlanetSideEmpire.Value
                  ): Behavior[Command] = {
    BuildingActor.setFactionTo(details, faction, log)
    val building = details.building
    building.Neighbours.getOrElse(Nil).foreach { _.Actor ! BuildingActor.AlertToFactionChange(building) }
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    Behaviors.same
  }

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  def powerLost(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  /**
    * Power has been restored.
    * All installed amenities are distributed a `PowerOn` message
    * and are instructed to display their "powered" model.
    * Additionally, the facility is now rendered spawnable if its player spawning amenities are online.
    */
  def powerRestored(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Offer(_, _) =>
        Behaviors.same
      case Request(amount, replyTo) =>
        //cavern stuff get free repairs
        replyTo ! NtuCommand.Grant(details.asInstanceOf[FacilityWrapper].supplier, amount)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }
}
