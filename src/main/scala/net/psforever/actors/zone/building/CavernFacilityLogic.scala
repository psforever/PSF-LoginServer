// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails, ZoneActor}
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.support.HackClearActor
import net.psforever.services.local.ClearMessage
import net.psforever.types.PlanetSideEmpire

/**
  * The logic that governs facilities and structures found in the cavern regions.
  */
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

  /**
    * Although cavern facilities don't possess many amenities that can be abused by faction enemies
    * or need to be statused on the continental map,
    * the facilities can be captured and controlled by a particular empire.
    * @param details package class that conveys the important information
    * @param entity the installed `Amenity` entity
    * @param data optional information
    * @return the next behavior for this control agency messaging system
    */
  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    entity match {
      case terminal: CaptureTerminal =>
        val building = details.building
        // Notify amenities that listen for CC hack state changes, e.g. wall turrets to dismount seated players
        data match {
          case Some(isResecured: Boolean) =>
            //pass hack information to amenities
            building.Amenities.filter(x => x.isInstanceOf[CaptureTerminalAware]).foreach(amenity => {
              amenity.Actor ! CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured)
            })
          case _ =>
            log(details).warn("CaptureTerminal AmenityStateChange was received with no attached data.")
        }
        // When a CC is hacked (or resecured) all currently hacked amenities for the base should return to their default unhacked state
        building.HackableAmenities.foreach(amenity => {
          if (amenity.HackedBy.isDefined) {
            building.Zone.LocalEvents ! ClearMessage(HackClearActor.ObjectIsResecured(amenity))
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
    val gates: Iterable[Building] = building.Zone.Buildings.values.filter(_.BuildingType == StructureType.WarpGate)
    gates.foreach { g =>
      val neighbors = g.Neighbours.getOrElse(Nil)
      neighbors.collect {
        case otherWg: Building => otherWg
      }
      .filter(_.Zone != g.Zone)
      .foreach { otherGate => otherGate.Zone.actor ! ZoneActor.ZoneMapUpdate()
      }
    }
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    Behaviors.same
  }

  /**
    * Cavern facilities get free auto-repair and give out free nanites.
    * Do they even care about nanites storage down there?
    * @param details package class that conveys the important information
    * @param msg the original message that instigated this upoate
    * @return the next behavior for this control agency messaging system
    */
  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Request(amount, replyTo) =>
        replyTo ! NtuCommand.Grant(details.asInstanceOf[FacilityWrapper].supplier, amount)

      case _ => ;
    }
    Behaviors.same
  }
}
