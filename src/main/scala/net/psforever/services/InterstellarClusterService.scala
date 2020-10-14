package net.psforever.services

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.{Player, SpawnPoint, Vehicle}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, SpawnGroup, Vector3}
import net.psforever.util.Config

import scala.collection.mutable
import scala.util.Random

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
}

class InterstellarClusterService(context: ActorContext[InterstellarClusterService.Command], _zones: Iterable[Zone])
    extends AbstractBehavior[InterstellarClusterService.Command](context) {

  import InterstellarClusterService._

  private[this] val log = org.log4s.getLogger

  val zoneActors: mutable.Map[String, (ActorRef[ZoneActor.Command], Zone)] = mutable.Map(
    _zones.map {
      case zone =>
        val zoneActor = context.spawn(ZoneActor(zone), s"zone-${zone.id}")
        (zone.id, (zoneActor, zone))
    }.toSeq: _*
  )

  val zones = zoneActors.map {
    case (id, (_, zone)) => zone
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
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
            case ((_, spot1, _), (_, spot2, _)) =>
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
          case Some(zone) =>
            /*
            val location = math.abs(Random.nextInt() % 4) match {
              case 0 => Vector3(sanctuary.map.Scale.width, sanctuary.map.Scale.height, 0) //NE
              case 1 => Vector3(sanctuary.map.Scale.width, 0, 0)                          //SE
              case 2 => Vector3.Zero                                                      //SW
              case 3 => Vector3(0, sanctuary.map.Scale.height, 0)                         //NW
            }
            sanctuary.findNearestSpawnPoints(
              faction,
              location,
              structures
            ) */
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

      case GetSpawnPoint(zoneNumber, player, target, replyTo) =>
        zones.find(_.Number == zoneNumber) match {
          case Some(zone) =>
            zone.findSpawns(player.Faction, SpawnGroup.values).find {
              case (spawn: Building, spawnPoints) =>
                spawn.MapId == target.guid || spawnPoints.exists(_.GUID == target)
              case (spawn: Vehicle, spawnPoints) =>
                spawn.GUID == target || spawnPoints.exists(_.GUID.guid == target.guid)
              case _ => false
            } match {
              case Some((_, spawnPoints)) =>
                replyTo ! SpawnPointResponse(Some(zone, Random.shuffle(spawnPoints.toList).head))
              case _ =>
                replyTo ! SpawnPointResponse(None)
            }
          case None =>
            replyTo ! SpawnPointResponse(None)
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
    }

    this
  }

}
