package net.psforever.actors.zone

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.{actor => classic}
import net.psforever.actors.commands.NtuCommand
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.persistence
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Database._
import services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.{InterstellarClusterService, ServiceManager}

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

  final case class Ntu(command: NtuCommand.Command) extends Command
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
      case (Some(galaxyService), Some(interstellarCluster)) =>
        buffer.unstashAll(active(galaxyService, interstellarCluster))
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

      case Ntu(msg) =>
        ntu(msg)
    }
  }

  def ntu(msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    val ntuBuilding = building match {
      case b: WarpGate => b
      case _           => return Behaviors.unhandled
    }

    msg match {
      case Offer(source) =>
      case Request(amount, replyTo) =>
        ntuBuilding match {
          case warpGate: WarpGate => replyTo ! Grant(warpGate, if (warpGate.Active) amount else 0)
          case _                  => return Behaviors.unhandled
        }

    }

    Behaviors.same
  }

}
