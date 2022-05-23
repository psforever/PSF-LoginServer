package net.psforever.services

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.{Player, SpawnPoint, Vehicle}
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.DroppodError
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, SpawnGroup, Vector3}
import net.psforever.util.Config
import net.psforever.zones.Zones

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Random, Success}

object InterstellarClusterService {
  val InterstellarClusterServiceKey: ServiceKey[Command] =
    ServiceKey[InterstellarClusterService.Command]("interstellarCluster")

  def apply(zones: Iterable[Zone]): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.setup { context =>
          context.system.receptionist ! Receptionist.Register(InterstellarClusterServiceKey, context.self)
          new InterstellarClusterService(context, zones)
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class FindZoneActor(predicate: Zone => Boolean, replyTo: ActorRef[ZoneActorResponse]) extends Command

  final case class ZoneActorResponse(zoneActor: Option[ActorRef[ZoneActor.Command]])

  final case class FindZone(predicate: Zone => Boolean, replyTo: ActorRef[ZoneResponse]) extends Command

  final case class ZoneResponse(zoneActor: Option[Zone])

  final case class FilterZones(predicate: Zone => Boolean, replyTo: ActorRef[ZonesResponse]) extends Command

  final case class ZonesResponse(zoneActor: Iterable[Zone])

  final case class GetInstantActionSpawnPoint(faction: PlanetSideEmpire.Value, replyTo: ActorRef[SpawnPointResponse])
      extends Command

  final case class GetSpawnPoint(
      zoneNumber: Int,
      player: Player,
      target: PlanetSideGUID,
      fromZoneNumber: Int,
      fromGateGuid: PlanetSideGUID,
      replyTo: ActorRef[SpawnPointResponse]
  ) extends Command

  final case class GetNearbySpawnPoint(
      zoneNumber: Int,
      player: Player,
      spawnGroups: Seq[SpawnGroup],
      replyTo: ActorRef[SpawnPointResponse]
  ) extends Command

  final case class GetRandomSpawnPoint(
      zoneNumber: Int,
      faction: PlanetSideEmpire.Value,
      spawnGroups: Seq[SpawnGroup],
      replyTo: ActorRef[SpawnPointResponse]
  ) extends Command

  final case class SpawnPointResponse(response: Option[(Zone, SpawnPoint)])

  final case class GetPlayers(replyTo: ActorRef[PlayersResponse]) extends Command

  final case class PlayersResponse(players: Seq[Avatar])

  final case class DroppodLaunchRequest(
                                         zoneNumber: Int,
                                         position: Vector3,
                                         faction: PlanetSideEmpire.Value,
                                         replyTo: ActorRef[DroppodLaunchExchange]
                                       ) extends Command

  final case class CavernRotation(msg: CavernRotationService.Command) extends Command

  trait DroppodLaunchExchange

  final case class DroppodLaunchConfirmation(destination: Zone, position: Vector3) extends DroppodLaunchExchange

  final case class DroppodLaunchDenial(errorCode: DroppodError, data: Option[Any]) extends DroppodLaunchExchange

  private case class ReceptionistListing(listing: Receptionist.Listing) extends Command
}

class InterstellarClusterService(context: ActorContext[InterstellarClusterService.Command], _zones: Iterable[Zone])
  extends AbstractBehavior[InterstellarClusterService.Command](context) {

  import InterstellarClusterService._

  private[this] val log = org.log4s.getLogger
  var intercontinentalSetup: Boolean = false
  var cavernRotation: Option[ActorRef[CavernRotationService.Command]] = None

  val zoneActors: mutable.Map[String, (ActorRef[ZoneActor.Command], Zone)] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    //setup
    val zoneLoadedList = _zones.map { _.ZoneInitialized() }
    val continentLinkFunc: ()=>Unit = MakeIntercontinentalLattice(
      zoneLoadedList.toList,
      context.system.receptionist,
      context.messageAdapter[Receptionist.Listing](ReceptionistListing)
    )
    zoneLoadedList.foreach {
      _.onComplete({
        case Success(true) => continentLinkFunc()
        case _ => ;
      })
    }
    //execute
    mutable.Map(
      _zones.map {
        zone =>
          val zoneActor = context.spawn(ZoneActor(zone), s"zone-${zone.id}")
          (zone.id, (zoneActor, zone))
      }.toSeq: _*
    )
  }

  val zones: Iterable[Zone] = zoneActors.map {
    case (_, (_, zone: Zone)) => zone
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case ReceptionistListing(CavernRotationService.CavernRotationServiceKey.Listing(listings)) =>
        listings.headOption match {
          case Some(ref) =>
            cavernRotation = Some(ref)
            ref ! CavernRotationService.ManageCaverns(zones)
          case None =>
            context.system.receptionist ! Receptionist.Find(
              CavernRotationService.CavernRotationServiceKey,
              context.messageAdapter[Receptionist.Listing](ReceptionistListing)
            )
        }

      case GetPlayers(replyTo) =>
        replyTo ! PlayersResponse(zones.flatMap(_.Players).toSeq)

      case FindZoneActor(predicate, replyTo) =>
        replyTo ! ZoneActorResponse(
          zoneActors.collectFirst {
            case (_, (actor, zone)) if predicate(zone) => actor
          }
        )

      case FindZone(predicate, replyTo) =>
        replyTo ! ZoneResponse(zones.find(predicate))

      case FilterZones(predicate, replyTo) =>
        replyTo ! ZonesResponse(zones.filter(predicate))

      case GetInstantActionSpawnPoint(faction, replyTo) =>
        val res = zones
          .filter(_.Players.nonEmpty)
          .flatMap { zone =>
            zone.HotSpotData.collect {
              case spot => (zone, spot)
            }
          }
          .map {
            case (zone, spot) =>
              (
                zone,
                spot,
                zone.findNearestSpawnPoints(
                  faction,
                  spot.DisplayLocation,
                  if (Config.app.game.instantActionAms) Seq(SpawnGroup.Tower, SpawnGroup.Facility, SpawnGroup.AMS)
                  else Seq(SpawnGroup.Tower, SpawnGroup.Facility)
                )
              )
          }
          .collect {
            case (zone, info, Some(spawns)) => (zone, info, spawns)
          }
          .toList
          .sortBy { case (_, spot, _) => spot.Activity.values.foldLeft(0)(_ + _.Heat) }(
            Ordering[Int].reverse
          ) // greatest > least
          .sortWith {
            case ((_, spot1, _), (_, _, _)) =>
              spot1.ActivityBy().contains(faction) // prefer own faction activity
          }
          .headOption
          .flatMap {
            case (zone, info, spawns) =>
              val pos        = info.DisplayLocation
              val spawnPoint = spawns.minBy(point => Vector3.DistanceSquared(point.Position, pos))
              //Some(zone, pos, spawnPoint)
              Some(zone, spawnPoint)
            case _ => None
          }
        replyTo ! SpawnPointResponse(res)

      case GetRandomSpawnPoint(zoneNumber, faction, spawnGroups, replyTo) =>
        val response = zones.find(_.Number == zoneNumber) match {
          case Some(zone: Zone) =>
            Random.shuffle(zone.findSpawns(faction, spawnGroups)).headOption match {
              case Some((_, spawnPoints)) if spawnPoints.nonEmpty =>
                Some((zone, Random.shuffle(spawnPoints.toList).head))
              case _ =>
                None
            }
          case None =>
            log.error(s"no zone $zoneNumber")
            None
        }
        replyTo ! SpawnPointResponse(response)

      case GetSpawnPoint(zoneNumber, player, target, fromZoneNumber, fromOriginGuid, replyTo) =>
        zones.find(_.Number == zoneNumber) match {
          case Some(zone) =>
            //found target zone; find a spawn point in target zone
            zone.findSpawns(player.Faction, SpawnGroup.values).find {
              case (spawn: Building, spawnPoints) =>
                spawn.MapId == target.guid || spawnPoints.exists(_.GUID == target)
              case (spawn: Vehicle, spawnPoints) =>
                spawn.GUID == target || spawnPoints.exists(_.GUID.guid == target.guid)
              case _ => false
            } match {
              case Some((_, spawnPoints)) =>
                //spawn point selected
                replyTo ! SpawnPointResponse(Some(zone, Random.shuffle(spawnPoints.toList).head))
              case _ =>
                //no spawn point found
                replyTo ! SpawnPointResponse(None)
            }
          case None =>
            //target zone not found; find origin and plot next immediate destination
            //applies to transit across intercontinental lattice
            (((zones.find(_.Number == fromZoneNumber) match {
              case Some(zone) => zone.GUID(fromOriginGuid)
              case _ => None
            }) match {
              case Some(warpGate: WarpGate) => warpGate.Neighbours //valid for warp gates only right now
              case _ => None
            }) match {
              case Some(neighbors) => neighbors.find(_ match { case _: WarpGate => true; case _ => false })
              case _ => None
            }) match {
              case Some(outputGate: WarpGate) =>
                //destination (next direct stopping point) found
                replyTo ! SpawnPointResponse(Some(outputGate.Zone, outputGate))
              case _ =>
                //no destination found
                replyTo ! SpawnPointResponse(None)
            }
        }

      case GetNearbySpawnPoint(zoneNumber, player, spawnGroups, replyTo) =>
        zones.find(_.Number == zoneNumber) match {
          case Some(zone) =>
            zone.findNearestSpawnPoints(player.Faction, player.Position, spawnGroups) match {
              case None | Some(Nil) =>
                replyTo ! SpawnPointResponse(None)
              case Some(spawnPoints) =>
                replyTo ! SpawnPointResponse(Some(zone, scala.util.Random.shuffle(spawnPoints).head))
            }
          case None =>
            replyTo ! SpawnPointResponse(None)
        }

      case DroppodLaunchRequest(zoneNumber, position, faction, replyTo) =>
        zones.find(_.Number == zoneNumber) match {
          case Some(zone) =>
            //TODO all of the checks for the specific DroppodLaunchResponseMessage excuses go here
            if(zone.map.cavern) {
              //just being cautious - caverns are typically not normally selectable as drop zones
              replyTo ! DroppodLaunchDenial(DroppodError.ZoneNotAvailable, None)
            } else if (zone.Number == Zones.sanctuaryZoneNumber(faction)) {
              replyTo ! DroppodLaunchDenial(DroppodError.OwnFactionLocked, None)
            } else {
              replyTo ! DroppodLaunchConfirmation(zone, position)
            }
          case None =>
            replyTo ! DroppodLaunchDenial(DroppodError.InvalidLocation, None)
        }

      case CavernRotation(rotationMsg) =>
        cavernRotation match {
          case Some(rotation) => rotation ! rotationMsg
          case None => ;
        }
    }

    this
  }

  private def MakeIntercontinentalLattice(
                                           flags: List[Future[Boolean]],
                                           receptionist: ActorRef[Receptionist.Command],
                                           adapter: ActorRef[Receptionist.Listing]
                                         )(): Unit = {
    if (flags.forall {
      _.value.contains(Success(true))
    } && !intercontinentalSetup) {
      intercontinentalSetup = true
      //intercontinental lattice setup
      _zones.foreach { zone =>
        zone.map.latticeLink
          .filter {
            case (a, _) => a.contains("/") // only intercontinental lattice connections
          }
          .map {
            case (source, target) =>
              val thisBuilding = source.split("/")(1)
              val (otherZone, otherBuilding) = target.split("/").take(2) match {
                case Array(a : String, b : String) => (a, b)
                case _ => ("", "")
              }
              (_zones.find {
                _.id.equals(otherZone)
              } match {
                case Some(_otherZone) => (zone.Building(thisBuilding), _otherZone.Building(otherBuilding), _otherZone)
                case None => (None, None, Zone.Nowhere)
              }) match {
                case (Some(sourceBuilding), Some(targetBuilding), _otherZone) =>
                  zone.AddIntercontinentalLatticeLink(sourceBuilding, targetBuilding)
                  _otherZone.AddIntercontinentalLatticeLink(targetBuilding, sourceBuilding)
                case (a, b, _) =>
                  log.error(s"InterstellarCluster: can't create lattice link between $source (${a.nonEmpty}) and $target (${b.nonEmpty})")
              }
          }
      }
      //error checking; almost all warp gates should be paired with at least one other gate
      // exception: inactive warp gates are not guaranteed to be connected
      // exception: the broadcast gates on sanctuary do not have partners
      // exception: the cavern gates are not be connected by default (see below)
      _zones.foreach { zone =>
        zone.Buildings.values
          .collect { case gate : WarpGate if gate.Active => gate }
          .filterNot { gate => gate.AllNeighbours.getOrElse(Nil).exists(_.isInstanceOf[WarpGate]) || !gate.Active || gate.Broadcast }
          .foreach { gate =>
            log.error(s"InterstellarCluster: found degenerate intercontinental lattice link - no paired warp gate for ${zone.id} ${gate.Name}")
          }
      }
      //error checking: connections between above-ground geowarp gates and subterranean cavern gates should exist
      if (Zones.cavernLattice.isEmpty) {
        log.error("InterstellarCluster: did not parse lattice connections for caverns")
      } else {
        Zones.cavernLattice.values.flatten.foreach { pair =>
          val a = pair.head
          val b = pair.last
          val (zone1: String, gate1: String) = {
            val raw = a.split("/").take(2)
            (raw.head, raw.last)
          }
          val (zone2: String, gate2: String) = {
            val raw = b.split("/").take(2)
            (raw.head, raw.last)
          }
          ((_zones.find(_.id.equals(zone1)), _zones.find(_.id.equals(zone2))) match {
            case (Some(z1), Some(z2)) => (z1.Building(gate1), z2.Building(gate2))
            case _ => (None, None)
          }) match {
            case (Some(_), Some(_)) => ;
            case _ =>
              log.error(s"InterstellarCluster: can't create cavern lattice link between $a and $b")
          }
        }
        //manage
        receptionist ! Receptionist.Find(CavernRotationService.CavernRotationServiceKey, adapter)
      }
    }
  }
}
