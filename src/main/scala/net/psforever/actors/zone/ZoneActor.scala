package net.psforever.actors.zone

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.structures.{StructureType, WarpGate}
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.blockmap.{BlockMapEntity, SectorGroup}
import net.psforever.objects.{ConstructionItem, PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.building.MajorFacilityLogic
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalAwareBehavior
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.{InGameActivity, InGameHistory}
import net.psforever.objects.zones.exp.{ExperienceCalculator, SupportExperienceCalculator}
import net.psforever.util.Database._
import net.psforever.persistence

import scala.collection.mutable
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ZoneActor {
  def apply(zone: Zone): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.setup(context => new ZoneActor(context, zone))
      }
      .onFailure[Exception](SupervisorStrategy.resume)

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

  final case class BuildDeployable(obj: Deployable, withTool: ConstructionItem)
      extends Command

  final case class DismissDeployable(obj: Deployable) extends Command

  final case class SpawnVehicle(vehicle: Vehicle) extends Command

  final case class DespawnVehicle(vehicle: Vehicle) extends Command

  final case class AddToBlockMap(target: BlockMapEntity, toPosition: Vector3) extends Command

  final case class UpdateBlockMap(target: BlockMapEntity, toPosition: Vector3) extends Command

  final case class RemoveFromBlockMap(target: BlockMapEntity) extends Command

  final case class ChangedSectors(addedTo: SectorGroup, removedFrom: SectorGroup)

  final case class HotSpotActivity(defender: SourceEntry, attacker: SourceEntry, location: Vector3) extends Command

  // TODO remove
  // Changes to zone objects should go through ZoneActor
  // Once they do, we won't need this anymore
  final case class ZoneMapUpdate() extends Command

  final case class RewardThisDeath(entity: PlanetSideGameObject with FactionAffinity with InGameHistory) extends Command

  final case class RewardOurSupporters(target: SourceEntry, history: Iterable[InGameActivity], kill: Kill, bep: Long) extends Command
}

class ZoneActor(context: ActorContext[ZoneActor.Command], zone: Zone)
  extends AbstractBehavior[ZoneActor.Command](context) {

  import ZoneActor._
  import ctx._

  private[this] val log           = org.log4s.getLogger
  private val players: mutable.ListBuffer[Player] = mutable.ListBuffer()
  private val experience: ActorRef[ExperienceCalculator.Command] = context.spawnAnonymous(ExperienceCalculator(zone))
  private val supportExperience: ActorRef[SupportExperienceCalculator.Command] = context.spawnAnonymous(SupportExperienceCalculator(zone))

  zone.actor = context.self
  zone.init(context.toClassic)

  ctx.run(query[persistence.Building].filter(_.zoneId == lift(zone.Number))).onComplete {
    case Success(buildings) =>
      buildings.foreach { building =>
        zone.BuildingByMapId(building.localId) match {
          case Some(_: WarpGate) => ()
            //warp gates are controlled by game logic and are better off not restored via the database
          case Some(b) =>
            if ((b.Faction = PlanetSideEmpire(building.factionId)) != PlanetSideEmpire.NEUTRAL) {
              b.ForceDomeActive = MajorFacilityLogic.checkForceDomeStatus(b).getOrElse(false)
              b.Neighbours.getOrElse(Nil).foreach(_.Actor ! BuildingActor.AlertToFactionChange(b))
              b.CaptureTerminal.collect { terminal =>
                val msg = CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured = true)
                b.Amenities.collect { case turret: FacilityTurret => turret.Actor ! msg }
              }
            }
          case None => ()
          // TODO this happens during testing, need a way to not always persist during tests
        }
      }
    case Failure(e) => log.error(e.getMessage)
  }

  def onMessage(msg: Command): Behavior[Command] = {
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

      case BuildDeployable(obj, _) =>
        zone.Deployables ! Zone.Deployable.Build(obj)

      case DismissDeployable(obj) =>
        zone.Deployables ! Zone.Deployable.Dismiss(obj)

      case SpawnVehicle(vehicle) =>
        zone.Transport ! Zone.Vehicle.Spawn(vehicle)

      case DespawnVehicle(vehicle) =>
        zone.Transport ! Zone.Vehicle.Despawn(vehicle)

      case AddToBlockMap(target, toPosition) =>
        zone.blockMap.addTo(target, toPosition)

      case UpdateBlockMap(target, toPosition) =>
        zone.blockMap.move(target, toPosition)

      case RemoveFromBlockMap(target) =>
        zone.blockMap.removeFrom(target)

      case HotSpotActivity(defender, attacker, location) =>
        zone.Activity ! Zone.HotSpot.Activity(defender, attacker, location)

      case RewardThisDeath(entity) =>
        experience ! ExperienceCalculator.RewardThisDeath(entity)

      case RewardOurSupporters(target, history, kill, bep) =>
        supportExperience ! SupportExperienceCalculator.RewardOurSupporters(target, history, kill, bep)

      case ZoneMapUpdate() =>
        zone.Buildings
          .filter(_._2.BuildingType == StructureType.Facility)
          .values
          .foreach(_.Actor ! BuildingActor.MapUpdate())
    }
    this
  }
}
