package net.psforever.actors.zone

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{ConstructionItem, PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.collection.mutable.ListBuffer
import akka.actor.typed.scaladsl.adapter._
import net.psforever.util.Database._
import net.psforever.persistence

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ZoneActor {
  def apply(zone: Zone): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.setup(context => new ZoneActor(context, zone))
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class GetZone(replyTo: ActorRef[ZoneResponse]) extends Command

  final case class ZoneResponse(zone: Zone)
  /*
  final case class AddAvatar(avatar: Avatar) extends Command

  final case class RemoveAvatar(avatar: Avatar) extends Command
   */
  final case class AddPlayer(player: Player) extends Command

  final case class RemovePlayer(player: Player) extends Command

  final case class DropItem(item: Equipment, position: Vector3, orientation: Vector3) extends Command

  final case class PickupItem(guid: PlanetSideGUID) extends Command

  final case class BuildDeployable(obj: PlanetSideGameObject with Deployable, withTool: ConstructionItem)
      extends Command

  final case class DismissDeployable(obj: PlanetSideGameObject with Deployable) extends Command

  final case class SpawnVehicle(vehicle: Vehicle) extends Command

  final case class DespawnVehicle(vehicle: Vehicle) extends Command

  final case class HotSpotActivity(defender: SourceEntry, attacker: SourceEntry, location: Vector3) extends Command

  // TODO remove
  // Changes to zone objects should go through ZoneActor
  // Once they do, we won't need this anymore
  final case class ZoneMapUpdate() extends Command

}

class ZoneActor(context: ActorContext[ZoneActor.Command], zone: Zone)
    extends AbstractBehavior[ZoneActor.Command](context) {

  import ZoneActor._
  import ctx._

  private[this] val log           = org.log4s.getLogger
  val players: ListBuffer[Player] = ListBuffer()

  zone.actor = context.self
  zone.init(context.toClassic)

  ctx.run(query[persistence.Building].filter(_.zoneId == lift(zone.Number))).onComplete {
    case Success(buildings) =>
      var capitol: Option[Building] = None
      buildings.foreach { building =>
        zone.BuildingByMapId(building.localId) match {
          case Some(b) =>
            b.Faction = PlanetSideEmpire(building.factionId)
            if(b.IsCapitol) {
              capitol = Some(b)
            }
          case None    =>
          // TODO this happens during testing, need a way to not always persist during tests
        }
      }
      capitol match {
        case Some(b) => b.ForceDomeActive = BuildingActor.checkForceDomeStatus(b).getOrElse(false)
        case None    => ;
      }
    case Failure(e) => log.error(e.getMessage)
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetZone(replyTo) =>
        replyTo ! ZoneResponse(zone)

      case AddPlayer(player) =>
        players.addOne(player)

      case RemovePlayer(player) =>
        players.filterInPlace(p => p.CharId == player.CharId)

      case DropItem(item, position, orientation) =>
        zone.Ground ! Zone.Ground.DropItem(item, position, orientation)

      case PickupItem(guid) =>
        zone.Ground ! Zone.Ground.PickupItem(guid)

      case BuildDeployable(obj, tool) =>
        zone.Deployables ! Zone.Deployable.Build(obj, tool)

      case DismissDeployable(obj) =>
        zone.Deployables ! Zone.Deployable.Dismiss(obj)

      case SpawnVehicle(vehicle) =>
        zone.Transport ! Zone.Vehicle.Spawn(vehicle)

      case DespawnVehicle(vehicle) =>
        zone.Transport ! Zone.Vehicle.Despawn(vehicle)

      case HotSpotActivity(defender, attacker, location) =>
        zone.Activity ! Zone.HotSpot.Activity(defender, attacker, location)

      case ZoneMapUpdate() =>
        zone.Buildings
          .filter(_._2.BuildingType == StructureType.Facility)
          .values
          .foreach(_.Actor ! BuildingActor.MapUpdate())
    }

    this
  }
}
