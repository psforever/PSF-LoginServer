// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails}
import net.psforever.objects.NtuContainer
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.services.InterstellarClusterService
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

final case class FacilityWrapper(
                                  building: Building,
                                  context: ActorContext[BuildingActor.Command],
                                  galaxyService: classic.ActorRef,
                                  interstellarCluster: ActorRef[InterstellarClusterService.Command]
                                )
  extends BuildingWrapper {
  val supplier = new FakeNtuSource(building)
}

case object FacilityLogic
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

  def setFactionTo(details: BuildingWrapper, faction : PlanetSideEmpire.Value): Behavior[Command] = {
    BuildingActor.setFactionTo(details, faction, log)
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    Behaviors.same
  }

  def powerLost(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def powerRestored(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Offer(_, _) =>
        Behaviors.same
      case Request(amount, replyTo) =>
        //towers and cavern stuff get free repairs
        replyTo ! NtuCommand.Grant(details.asInstanceOf[FacilityWrapper].supplier, amount)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }
}

protected class FakeNtuSource(private val building: Building)
  extends PlanetSideServerObject
    with NtuContainer {
  override def NtuCapacitor = Int.MaxValue.toFloat
  override def NtuCapacitor_=(a: Float) = Int.MaxValue.toFloat
  override def MaxNtuCapacitor = Int.MaxValue.toFloat
  override def Faction = building.Faction
  override def Zone = building.Zone
  override def Definition = null
}
