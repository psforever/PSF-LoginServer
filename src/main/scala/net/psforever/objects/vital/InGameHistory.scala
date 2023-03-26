// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{EquipmentDefinition, KitDefinition, ToolDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{AmenitySource, ObjectSource, PlayerSource, SourceEntry, SourceWithHealthEntry, VehicleSource}
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.{ExplodingEntityReason, PainboxReason, SuicideReason}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.types.{ExoSuitType, ImplantType, TransactionType}

/* root */

/**
 * The root of all chronological activity.
 * Must keep track of the time (ms) the activity occurred.
 */
trait InGameActivity {
  val time: Long = System.currentTimeMillis()
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

final case class SpawningActivity(src: SourceEntry, zoneNumber: Int, unit: Option[SourceEntry])
  extends GeneralActivity

final case class ReconstructionActivity(src: SourceEntry, zoneNumber: Int, unit: Option[SourceEntry])
  extends GeneralActivity

final case class RevivingActivity(target: SourceEntry, user: PlayerSource, amount: Int, equipment: EquipmentDefinition)
  extends GeneralActivity with SupportActivityCausedByAnother

final case class ShieldCharge(amount: Int, cause: Option[SourceEntry])
  extends GeneralActivity

final case class TerminalUsedActivity(terminal: AmenitySource, transaction: TransactionType.Value)
  extends GeneralActivity

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
      case (pb: PlayerSource, pa: PlayerSource) if pb.ExoSuit == ExoSuitType.MAX => pb.total - pa.total
      case (pb: SourceWithHealthEntry, pa: SourceWithHealthEntry)                => pb.health - pa.health
      case _                                                                     => 0
    }
  }
}

final case class HealFromKit(kit_def: KitDefinition, amount: Int)
  extends HealingActivity

final case class HealFromEquipment(user: PlayerSource, equipment_def: EquipmentDefinition, amount: Int)
  extends HealingActivity with SupportActivityCausedByAnother

final case class HealFromTerm(term: AmenitySource, amount: Int)
  extends HealingActivity

final case class HealFromImplant(implant: ImplantType, amount: Int)
  extends HealingActivity

final case class RepairFromExoSuitChange(exosuit: ExoSuitType.Value, amount: Int)
  extends RepairingActivity

final case class RepairFromKit(kit_def: KitDefinition, amount: Int)
    extends RepairingActivity()

final case class RepairFromEquipment(user: PlayerSource, equipment_def: EquipmentDefinition, amount: Int)
  extends RepairingActivity with SupportActivityCausedByAnother

final case class RepairFromTerm(term: AmenitySource, amount: Int) extends RepairingActivity

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
   * @param action the fully-informed entry
   * @return the list of previous changes to this entity
   */
  def LogActivity(action: Option[InGameActivity]): List[InGameActivity] = {
    action match {
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
      case _ => ;
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

  def ClearHistory(): List[InGameActivity] = {
    lastDamage = None
    val out = history
    history = List.empty
    out
  }
}

object InGameHistory {
  def SpawnReconstructionActivity(
                                   obj: PlanetSideGameObject with FactionAffinity with InGameHistory,
                                   zoneNumber: Int,
                                   unit: Option[SourceEntry]
                                 ): Unit = {
    val event: GeneralActivity = if (obj.History.nonEmpty || obj.History.headOption.exists {
      _.isInstanceOf[SpawningActivity]
    }) {
      ReconstructionActivity(ObjectSource(obj), zoneNumber, unit)
    } else {
      SpawningActivity(ObjectSource(obj), zoneNumber, unit)
    }
    if (obj.History.lastOption match {
      case Some(evt: SpawningActivity) => evt != event
      case Some(evt: ReconstructionActivity) => evt != event
      case _ => true
    }) {
      obj.LogActivity(event)
    }
  }
}
