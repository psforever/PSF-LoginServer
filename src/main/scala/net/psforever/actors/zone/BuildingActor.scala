package net.psforever.actors.zone

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.{actor => classic}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.building._
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.persistence
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Database.ctx
import org.log4s.Logger

import scala.util.{Failure, Success}

object BuildingActor {
  def apply(zone: Zone, building: Building): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(100) { buffer =>
          val logic: BuildingLogic = building match {
            case _: WarpGate =>
              WarpGateLogic
            case _ if building.BuildingType == StructureType.Tower || zone.map.cavern =>
              FacilityLogic
            case _ =>
              MajorFacilityLogic
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

  def setFactionTo(
                    details: BuildingControlDetails,
                    faction: PlanetSideEmpire.Value,
                    log: BuildingControlDetails => Logger
                  ): Unit = {
    setFactionInDatabase(details, faction, log)
    setFactionOnEntity(details, faction, log)
  }

  def setFactionInDatabase(
                            details: BuildingControlDetails,
                            faction: PlanetSideEmpire.Value,
                            log: BuildingControlDetails => Logger
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

  def setFactionOnEntity(
                          details: BuildingControlDetails,
                          faction: PlanetSideEmpire.Value,
                          log: BuildingControlDetails => Logger
                        ): Unit = {
    val building = details.building
    val zone = building.Zone
    building.Faction = faction
    zone.actor ! ZoneActor.ZoneMapUpdate() // Update entire lattice to show lattice benefits
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
  var galaxyService: Option[classic.ActorRef]                                   = None
  var interstellarCluster: Option[ActorRef[InterstellarClusterService.Command]] = None
  var dets: Option[BuildingControlDetails] = None

  context.system.receptionist ! Receptionist.Find(
    InterstellarClusterService.InterstellarClusterServiceKey,
    context.messageAdapter[Receptionist.Listing](ReceptionistListing)
  )

  ServiceManager.serviceManager ! ServiceManager.LookupFromTyped(
    "galaxy",
    context.messageAdapter[ServiceManager.LookupResult](ServiceManagerLookupResult)
  )

  def start(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case ReceptionistListing(InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings)) =>
        interstellarCluster = listings.headOption
        postStartBehaviour()

      case ServiceManagerLookupResult(ServiceManager.LookupResult(request, endpoint)) =>
        request match {
          case "galaxy" => galaxyService = Some(endpoint)
        }
        postStartBehaviour()

      case other =>
        buffer.stash(other)
        Behaviors.same
    }
  }

  def postStartBehaviour(): Behavior[Command] = {
    (galaxyService, interstellarCluster) match {
      case (Some(_galaxyService), Some(_interstellarCluster)) =>
        val details = BuildingControlDetails(building, context, _galaxyService, _interstellarCluster)
        dets = Some(details)
        galaxyService = None
        interstellarCluster = None
        buffer.unstashAll(active(details))
      case _ =>
        Behaviors.same
    }
  }

  def active(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.receiveMessagePartial {
      case SetFaction(faction) =>
        logic.setFactionTo(details, faction)

      case AlertToFactionChange(neighbor) =>
        logic.alertToFactionChange(details, neighbor)
        Behaviors.same

      case MapUpdate() =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(details.building.infoUpdateMessage()))
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
    }
  }
}
