package net.psforever.actors.zone

import akka.{actor => classic}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.building._
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.ContinentalLockUpdateMessage
import net.psforever.persistence
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Database.ctx
import org.log4s.Logger

import scala.util.{Failure, Success}

final case class BuildingControlDetails(
                                         galaxyService: classic.ActorRef = null,
                                         interstellarCluster: ActorRef[InterstellarClusterService.Command] = null
                                       )

object BuildingActor {
  def apply(zone: Zone, building: Building): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(capacity = 100) { buffer =>
          val logic: BuildingLogic = building match {
            case _: WarpGate =>
              WarpGateLogic
            case _ if zone.map.cavern =>
              CavernFacilityLogic
            case _ if building.BuildingType == StructureType.Facility =>
              MajorFacilityLogic
            case _ =>
              FacilityLogic
          }
          Behaviors.setup(context => new BuildingActor(context, buffer, zone, building, logic).start())
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  private case class ReceptionistListing(listing: Receptionist.Listing) extends Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  final case class SetFaction(faction: PlanetSideEmpire.Value) extends Command

  final case class AlertToFactionChange(building: Building) extends Command

  // TODO remove
  // Changes to building objects should go through BuildingActor
  // Once they do, we won't need this anymore
  final case class MapUpdate() extends Command

  final case class AmenityStateChange(obj: Amenity, data: Option[Any]) extends Command

  object AmenityStateChange{
    def apply(obj: Amenity): AmenityStateChange = AmenityStateChange(obj, None)
  }

  final case class Ntu(command: NtuCommand.Command) extends Command

  final case class SuppliedWithNtu() extends Command

  final case class NtuDepleted() extends Command

  final case class PowerOn() extends Command

  final case class PowerOff() extends Command

  final case class DensityLevelUpdate(building: Building) extends Command

  final case class ContinentalLock(zone: Zone) extends Command

  final case class HomeLockBenefits(msg: PlanetSideGamePacket) extends Command
  /**
    * Set a facility affiliated to one faction to be affiliated to a different faction.
    * @param details building and event system references
    * @param faction faction to which the building is being set
    * @param log wrapped-up log for customized debug information
    */
  def setFactionTo(
                    details: BuildingWrapper,
                    faction: PlanetSideEmpire.Value,
                    log: BuildingWrapper => Logger
                  ): Unit = {
    setFactionInDatabase(details, faction, log)
    setFactionOnEntity(details, faction, log)
  }

  /**
    * Set a facility affiliated to one faction to be affiliated to a different faction.
    * Handle the database entry updates to reflect the proper faction affiliation.
    * @param details building and event system references
    * @param faction faction to which the building is being set
    * @param log wrapped-up log for customized debug information
    */
  def setFactionInDatabase(
                            details: BuildingWrapper,
                            faction: PlanetSideEmpire.Value,
                            log: BuildingWrapper => Logger
                          ): Unit = {
    val building = details.building
    val zone = building.Zone
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    ctx
      .run(
        query[persistence.Building]
          .filter(_.localId == lift(building.MapId))
          .filter(_.zoneId == lift(zone.Number))
      )
      .onComplete {
        case Success(res) =>
          res.headOption match {
            case Some(_) =>
              ctx
                .run(
                  query[persistence.Building]
                    .filter(_.localId == lift(building.MapId))
                    .filter(_.zoneId == lift(zone.Number))
                    .update(_.factionId -> lift(faction.id))
                )
                .onComplete {
                  case Success(_) =>
                  case Failure(e) => log(details).error(e.getMessage)
                }
            case _ =>
              ctx
                .run(
                  query[persistence.Building]
                    .insert(
                      _.localId -> lift(building.MapId),
                      _.factionId -> lift(faction.id),
                      _.zoneId -> lift(zone.Number)
                    )
                )
                .onComplete {
                  case Success(_) =>
                  case Failure(e) => log(details).error(e.getMessage)
                }
          }
        case Failure(e) => log(details).error(e.getMessage)
      }
  }

  /**
    * Set a facility affiliated to one faction to be affiliated to a different faction.
    * Handle the facility entry to reflect the correct faction affiliation.
    * @param details building and event system references
    * @param faction faction to which the building is being set
    * @param log wrapped-up log for customized debug information
    */
  def setFactionOnEntity(
                          details: BuildingWrapper,
                          faction: PlanetSideEmpire.Value,
                          log: BuildingWrapper => Logger
                        ): Unit = {
    val building = details.building
    val zone = building.Zone
    building.Faction = faction
    zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.SetEmpire(building.GUID, faction))
  }
}

class BuildingActor(
    context: ActorContext[BuildingActor.Command],
    buffer: StashBuffer[BuildingActor.Command],
    zone: Zone,
    building: Building,
    logic: BuildingLogic
) {
  import BuildingActor._

  def start(): Behavior[Command] = {
    context.system.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.messageAdapter[Receptionist.Listing](ReceptionistListing)
    )
    ServiceManager.serviceManager ! ServiceManager.LookupFromTyped(
      "galaxy",
      context.messageAdapter[ServiceManager.LookupResult](ServiceManagerLookupResult)
    )
    setup(BuildingControlDetails())
  }

  def setup(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.receiveMessage {
      case ReceptionistListing(InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings))
        if listings.isEmpty =>
        Behaviors.same

      case ReceptionistListing(InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings)) =>
        switchToBehavior(details.copy(interstellarCluster = listings.head))

      case ServiceManagerLookupResult(ServiceManager.LookupResult(request, endpoint)) =>
        switchToBehavior(request match {
          case "galaxy" => details.copy(galaxyService = endpoint)
          case _        => details
        })

      case other =>
        buffer.stash(other)
        setup(details)
    }
  }

  def switchToBehavior(details: BuildingControlDetails): Behavior[Command] = {
    if (details.galaxyService != null && details.interstellarCluster != null) {
      buffer.unstashAll(active(logic.wrapper(building, context, details)))
    } else {
      setup(details)
    }
  }

  def active(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.receiveMessagePartial {
      case SetFaction(faction) =>
        logic.setFactionTo(details, faction)

      case AlertToFactionChange(neighbor) =>
        logic.alertToFactionChange(details, neighbor)
        Behaviors.same

      case MapUpdate() =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(details.building.infoUpdateMessage()))
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(details.building.densityLevelUpdateMessage(building)))
        Behaviors.same

      case AmenityStateChange(amenity, data) =>
        logic.amenityStateChange(details, amenity, data)

      case PowerOff() =>
        logic.powerOff(details)

      case PowerOn() =>
        logic.powerOn(details)

      case NtuDepleted() =>
        logic.ntuDepleted(details)

      case SuppliedWithNtu() =>
        logic.suppliedWithNtu(details)

      case Ntu(msg) =>
        logic.ntu(details, msg)

      case DensityLevelUpdate(building) =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(details.building.densityLevelUpdateMessage(building)))
        Behaviors.same

      case ContinentalLock(zone) =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(ContinentalLockUpdateMessage(zone.Number, zone.lockedBy)))
        Behaviors.same

      case HomeLockBenefits(msg) =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.SendResponse(msg))
        Behaviors.same
    }
  }
}
