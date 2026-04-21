// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{EquipmentDefinition, KitDefinition, ToolDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{AmenitySource, DeployableSource, MountableEntry, PlayerSource, SourceEntry, SourceUniqueness, SourceWithHealthEntry, VehicleSource}
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.{ExplodingEntityReason, PainboxReason, SuicideReason}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.types.{ExoSuitType, ImplantType, TransactionType}
import net.psforever.util.Config

import scala.collection.mutable

/* root */

/**
 * The root of all chronological activity.
 * Must keep track of the time (ms) the activity occurred.
 */
trait InGameActivity {
  private var _time: Long = System.currentTimeMillis()

  def time: Long = _time
}

object InGameActivity {
  def ShareTime(benefactor: InGameActivity, donor: InGameActivity): InGameActivity = {
    benefactor._time = donor.time
    benefactor
  }

  def SetTime(benefactor: InGameActivity, time: Long): InGameActivity = {
    benefactor._time = time
    benefactor
  }
}

/* normal history */

/**
 * A generic classification of activity.
 */
trait GeneralActivity extends InGameActivity

trait SupportActivityCausedByAnother {
  def user: PlayerSource
  def amount: Int
}

trait ExoSuitChange {
  def exosuit: ExoSuitType.Value
}

trait CommonExoSuitChange extends ExoSuitChange {
  def src: SourceEntry

  def exosuit: ExoSuitType.Value = {
    src match {
      case p: PlayerSource => p.ExoSuit
      case _               => ExoSuitType.Standard
    }
  }
}

trait IncarnationActivity extends GeneralActivity

final case class SpawningActivity(src: SourceEntry, zoneNumber: Int, unit: Option[SourceEntry])
  extends IncarnationActivity with CommonExoSuitChange

final case class ReconstructionActivity(src: SourceEntry, zoneNumber: Int, unit: Option[SourceEntry])
  extends IncarnationActivity with CommonExoSuitChange

final case class RevivingActivity(target: SourceEntry, user: PlayerSource, amount: Int, equipment: EquipmentDefinition)
  extends IncarnationActivity with SupportActivityCausedByAnother

final case class ShieldCharge(amount: Int, cause: Option[SourceEntry])
  extends GeneralActivity

trait TerminalUse {
  def terminal: AmenitySource
}

final case class TerminalUsedActivity(terminal: AmenitySource, transaction: TransactionType.Value)
  extends GeneralActivity with TerminalUse

final case class TelepadUseActivity(router: VehicleSource, telepad: DeployableSource, player: PlayerSource)
  extends GeneralActivity

sealed trait MountChange extends GeneralActivity {
  def mount: SourceEntry with MountableEntry
  def zoneNumber: Int
}

sealed trait PassengerMountChange extends MountChange {
  def player: PlayerSource
}

sealed trait CargoMountChange extends MountChange {
  def cargo: VehicleSource
}

final case class MountingActivity(mount: SourceEntry with MountableEntry, player: PlayerSource, zoneNumber: Int)
  extends PassengerMountChange

final case class DismountingActivity(
                                      mount: SourceEntry with MountableEntry,
                                      player: PlayerSource,
                                      zoneNumber: Int,
                                      pairedEvent: Option[MountingActivity] = None
                                    ) extends PassengerMountChange

final case class VehicleCargoMountActivity(mount: VehicleSource, cargo: VehicleSource, zoneNumber: Int)
  extends CargoMountChange

final case class VehicleCargoDismountActivity(
                                               mount: VehicleSource,
                                               cargo: VehicleSource,
                                               zoneNumber: Int,
                                               pairedEvent: Option[VehicleCargoMountActivity] = None
                                             ) extends CargoMountChange

final case class Contribution(src: SourceUniqueness, entries: List[InGameActivity])
  extends GeneralActivity {
  val start: Long = entries.headOption.map { _.time }.getOrElse(System.currentTimeMillis())
  val end: Long = entries.lastOption.map { _.time }.getOrElse(start)
}

/* vitals history */

/**
 * A vital entity can be hurt or damaged or healed or repaired (HDHR).<br>
 * Shields are not included in the definition of what is a "vital statistic",
 * and that includes Infantry shields due to the Personal Shield implant
 * and MAX shields due to being a New Conglomerate soldier.
 */
trait VitalsActivity extends InGameActivity {
  def amount: Int
}

trait HealingActivity extends VitalsActivity

trait RepairingActivity extends VitalsActivity

trait DamagingActivity extends VitalsActivity {
  override val time: Long = data.interaction.hitTime
  def data: DamageResult

  def amount: Int = {
    (data.targetBefore, data.targetAfter) match {
      case (pb: SourceWithHealthEntry, pa: SourceWithHealthEntry) => pb.total - pa.total
      case _                                                      => 0
    }
  }

  def health: Int = {
    (data.targetBefore, data.targetAfter) match {
      case (pb: SourceWithHealthEntry, pa: SourceWithHealthEntry) => pb.health - pa.health
      case _                                                      => 0
    }
  }
}

final case class HealFromKit(kit_def: KitDefinition, amount: Int)
  extends HealingActivity

final case class HealFromEquipment(user: PlayerSource, equipment_def: EquipmentDefinition, amount: Int)
  extends HealingActivity with SupportActivityCausedByAnother

final case class HealFromTerminal(terminal: AmenitySource, amount: Int)
  extends HealingActivity with TerminalUse

final case class HealFromImplant(implant: ImplantType, amount: Int)
  extends HealingActivity

final case class RepairFromExoSuitChange(exosuit: ExoSuitType.Value, amount: Int)
  extends RepairingActivity with ExoSuitChange

final case class RepairFromKit(kit_def: KitDefinition, amount: Int)
    extends RepairingActivity()

final case class RepairFromEquipment(user: PlayerSource, equipment_def: EquipmentDefinition, amount: Int)
  extends RepairingActivity with SupportActivityCausedByAnother

final case class RepairFromTerminal(terminal: AmenitySource, amount: Int)
  extends RepairingActivity with TerminalUse

final case class RepairFromArmorSiphon(siphon_def: ToolDefinition, vehicle: VehicleSource, amount: Int)
  extends RepairingActivity

final case class RepairFromAmenityAutoRepair(amount: Int)
  extends RepairingActivity

final case class DamageFrom(data: DamageResult)
  extends DamagingActivity

final case class DamageFromProjectile(data: DamageResult)
  extends DamagingActivity

final case class DamageFromPainbox(data: DamageResult)
  extends DamagingActivity

final case class DamageFromEnvironment(data: DamageResult)
  extends DamagingActivity

final case class PlayerSuicide(player: PlayerSource)
  extends DamagingActivity {
  private lazy val result = {
    val out = DamageResult(
      player,
      player.copy(health = 0),
      DamageInteraction(player, SuicideReason(), player.Position)
    )
    out
  }
  def data: DamageResult = result
}

final case class DamageFromExplodingEntity(data: DamageResult)
  extends DamagingActivity


trait InGameHistory {
  /** a list of important events that have occurred in chronological order */
  private var history: List[InGameActivity] = List.empty[InGameActivity]
  /** the last source of damage that cna be used to indicate blame for damage */
  private var lastDamage: Option[DamageResult] = None

  def History: List[InGameActivity] = history

  /**
   * Only the changes to vitality statistics.
   * @return a list of the chronologically-consistent vitality events
   */
  def VitalsHistory(): List[VitalsActivity] = History.collect {
    case event: VitalsActivity => event
  }

  /**
   * An in-game event must be recorded.
   * Add new entry to the list (for recent activity).
   * @param action the fully-informed entry
   * @return the list of previous changes to this entity
   */
  def LogActivity(action: InGameActivity): List[InGameActivity] = LogActivity(Some(action))


  /**
   * An in-game event must be recorded.
   * Add new entry to the list (for recent activity).
   * Special handling must be conducted for certain events.
   * @param action the fully-informed entry
   * @return the list of previous changes to this entity
   */
  def LogActivity(action: Option[InGameActivity]): List[InGameActivity] = {
    action match {
      case Some(act: DismountingActivity) if act.pairedEvent.isEmpty =>
        history
          .findLast(_.isInstanceOf[MountingActivity])
          .collect {
            case event: MountingActivity if event.mount.unique == act.mount.unique =>
              history = history :+ InGameActivity.ShareTime(act.copy(pairedEvent = Some(event)), act)
          }
          .orElse {
            history = history :+ act
            None
          }
      case Some(act: DismountingActivity) =>
        history = history :+ act
      case Some(act: VehicleCargoDismountActivity) =>
        history
          .findLast(_.isInstanceOf[VehicleCargoMountActivity])
          .collect {
            case event: VehicleCargoMountActivity if event.mount.unique == act.mount.unique =>
              history = history :+ InGameActivity.ShareTime(act.copy(pairedEvent = Some(event)), act)
          }
          .orElse {
            history = history :+ act
            None
          }
      case Some(act) =>
        history = history :+ act
      case None => ()
    }
    history
  }

  /**
   * Very common example of a `VitalsActivity` event involving damage.
   * They are repackaged before submission and are often tagged for specific blame.
   * @param result the fully-informed entry
   * @return the list of previous changes to this object's vital statistics
   */
  def LogActivity(result: DamageResult): List[InGameActivity] = {
    result.interaction.cause match {
      case _: ProjectileReason =>
        LogActivity(DamageFromProjectile(result))
        lastDamage = Some(result)
      case _: ExplodingEntityReason =>
        LogActivity(DamageFromExplodingEntity(result))
        lastDamage = Some(result)
      case _: PainboxReason =>
        LogActivity(DamageFromPainbox(result))
      case _: EnvironmentReason =>
        LogActivity(DamageFromEnvironment(result))
      case _ =>
        LogActivity(DamageFrom(result))
        if(result.adversarial.nonEmpty) {
          lastDamage = Some(result)
        }
    }
    History
  }

  def LastDamage: Option[DamageResult] = lastDamage

  /**
   * Find, specifically, the last instance of a weapon discharge that caused damage.
   * @return information about the discharge
   */
  def LastShot: Option[DamageResult] = {
    History.findLast({ p => p.isInstanceOf[DamageFromProjectile] }).map {
      case entry: DamageFromProjectile => entry.data
    }
  }

  /**
   * activity that comes from another entity used for scoring;<br>
   * key - unique reference to that entity; value - history from that entity
   */
  private val contributionInheritance: mutable.HashMap[SourceUniqueness, Contribution] =
    mutable.HashMap[SourceUniqueness, Contribution]()

  def ContributionFrom(target: PlanetSideGameObject with FactionAffinity with InGameHistory): Option[Contribution] = {
    if (target eq this) {
      None
    } else {
      val uniqueTarget = SourceUniqueness(target)
      (target.GetContribution(), contributionInheritance.get(uniqueTarget)) match {
        case (Some(in), Some(curr)) =>
          val end = curr.end
          val contribution = Contribution(uniqueTarget, curr.entries ++ in.filter(_.time > end))
          contributionInheritance.put(uniqueTarget, contribution)
          Some(contribution)
        case (Some(in), _) =>
          val contribution = Contribution(uniqueTarget, in)
          contributionInheritance.put(uniqueTarget, contribution)
          Some(contribution)
        case (None, _) =>
          None
      }
    }
  }

  def GetContribution(): Option[List[InGameActivity]] = {
    Option(GetContributionDuringPeriod(History, duration = Config.app.game.experience.longContributionTime))
  }

  def GetContributionDuringPeriod(list: List[InGameActivity], duration: Long): List[InGameActivity] = {
    val earliestEndTime = System.currentTimeMillis() - duration
    list.collect {
      case event: DamagingActivity if event.health > 0 && event.time > earliestEndTime  => event
      case event: RepairingActivity if event.amount > 0 && event.time > earliestEndTime => event
    }
  }

  def HistoryAndContributions(): List[InGameActivity] = {
    History ++ contributionInheritance.values.toList
  }

  def ClearHistory(): List[InGameActivity] = {
    lastDamage = None
    val out = history
    history = List.empty
    contributionInheritance.clear()
    out
  }
}

object InGameHistory {
  def SpawnReconstructionActivity(
                                   obj: PlanetSideGameObject with FactionAffinity with InGameHistory,
                                   zoneNumber: Int,
                                   unit: Option[PlanetSideGameObject with FactionAffinity with InGameHistory]
                                 ): Unit = {
    val toUnitSource = unit.collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) }

    val event: GeneralActivity = if (obj.History.isEmpty) {
      SpawningActivity(SourceEntry(obj), zoneNumber, toUnitSource)
    } else {
      ReconstructionActivity(SourceEntry(obj), zoneNumber, toUnitSource)
    }
    if (obj.History.lastOption match {
      case Some(evt: SpawningActivity) => evt != event
      case Some(evt: ReconstructionActivity) => evt != event
      case _ => true
    }) {
      obj.LogActivity(event)
      unit.foreach { o => obj.ContributionFrom(o) }
    }
  }

  def ContributionFrom(target: PlanetSideGameObject with FactionAffinity with InGameHistory): Option[Contribution] = {
    target
      .GetContribution()
      .collect { case events => Contribution(SourceUniqueness(target), events) }
  }
}
