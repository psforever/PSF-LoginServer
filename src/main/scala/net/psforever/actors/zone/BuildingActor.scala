package net.psforever.actors.zone

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.{actor => classic}
import net.psforever.actors.commands.NtuCommand
import net.psforever.objects.{CommonNtuContainer, NtuContainer}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.persistence
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Database._
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.{InterstellarClusterService, ServiceManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object BuildingActor {
  def apply(zone: Zone, building: Building): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(100) { buffer =>
          Behaviors.setup(context => new BuildingActor(context, buffer, zone, building).start())
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  private case class ReceptionistListing(listing: Receptionist.Listing) extends Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  final case class SetFaction(faction: PlanetSideEmpire.Value) extends Command

  // TODO remove
  // Changes to building objects should go through BuildingActor
  // Once they do, we won't need this anymore
  final case class MapUpdate() extends Command

  final case class AmenityStateChange(obj: Amenity) extends Command

  final case class Ntu(command: NtuCommand.Command) extends Command

  final case class SuppliedWithNtu() extends Command

  final case class NtuDepleted() extends Command
}

class BuildingActor(
    context: ActorContext[BuildingActor.Command],
    buffer: StashBuffer[BuildingActor.Command],
    zone: Zone,
    building: Building
) {

  import BuildingActor._

  private[this] val log                                                         = org.log4s.getLogger
  var galaxyService: Option[classic.ActorRef]                                   = None
  var interstellarCluster: Option[ActorRef[InterstellarClusterService.Command]] = None

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
        buffer.unstashAll(active(_galaxyService, _interstellarCluster))
      case _ =>
        Behaviors.same
    }
  }

  def active(
      galaxyService: classic.ActorRef,
      interstellarCluster: ActorRef[InterstellarClusterService.Command]
  ): Behavior[Command] = {
    Behaviors.receiveMessagePartial {
      case SetFaction(faction) =>
        import ctx._
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
                        .update(_.factionId -> lift(building.Faction.id))
                    )
                    .onComplete {
                      case Success(_) =>
                      case Failure(e) => log.error(e.getMessage)
                    }
                case _ =>
                  ctx
                    .run(
                      query[persistence.Building]
                        .insert(
                          _.localId   -> lift(building.MapId),
                          _.factionId -> lift(building.Faction.id),
                          _.zoneId    -> lift(zone.Number)
                        )
                    )
                    .onComplete {
                      case Success(_) =>
                      case Failure(e) => log.error(e.getMessage)
                    }
              }
            case Failure(e) => log.error(e.getMessage)
          }
        building.Faction = faction
        galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(building.infoUpdateMessage()))
        zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.SetEmpire(building.GUID, faction))
        Behaviors.same

      case MapUpdate() =>
        galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(building.infoUpdateMessage()))
        Behaviors.same

      case AmenityStateChange(_) =>
        //TODO when parameter object is finally immutable, perform analysis on it to determine specific actions
        //for now, just update the map
        galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(building.infoUpdateMessage()))
        Behaviors.same

      case msg @ NtuDepleted() =>
        log.trace(s"${building.Definition.Name} ${building.Name} ntu has been depleted")
        building.Amenities.foreach { amenity =>
          amenity.Actor ! msg
        }
        Behaviors.same

      case msg @ SuppliedWithNtu() =>
        log.trace(s"ntu supply has been restored to ${building.Definition.Name} ${building.Name}")
        building.Amenities.foreach { amenity =>
          amenity.Actor ! msg
        }
        Behaviors.same

      case Ntu(msg) =>
        ntu(msg)
    }
  }

  def ntu(msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Offer(_, _) =>
        Behaviors.same
      case Request(amount, replyTo) =>
        building match {
          case b: WarpGate =>
            //warp gates are an infiite source of nanites
            replyTo ! Grant(b, if (b.Active) amount else 0)
            Behaviors.same
          case _ if building.BuildingType == StructureType.Tower || building.Zone.map.cavern =>
            //towers and cavern stuff get free repairs
            replyTo ! NtuCommand.Grant(new FakeNtuSource(building), amount)
            Behaviors.same
          case _           =>
            //all other facilities require a storage silo for ntu
            building.Amenities.find(_.isInstanceOf[NtuContainer]) match {
              case Some(ntuContainer) =>
                ntuContainer.Actor ! msg //needs to redirect
                Behaviors.same
              case None =>
                replyTo ! NtuCommand.Grant(null, 0)
                Behaviors.unhandled
            }
        }
      case _ =>
        Behaviors.same
    }
  }
}

class FakeNtuSource(private val building: Building)
  extends PlanetSideServerObject
  with CommonNtuContainer {
  override def NtuCapacitor = Float.MaxValue
  override def Faction = building.Faction
  override def Zone = building.Zone
  override def Definition = null
}
