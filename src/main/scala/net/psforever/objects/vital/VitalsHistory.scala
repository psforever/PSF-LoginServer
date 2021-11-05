// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.{PlayerSource, VehicleSource}
import net.psforever.objects.definition.{EquipmentDefinition, KitDefinition, ToolDefinition}
import net.psforever.objects.serverobject.terminals.TerminalDefinition
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.{ExplodingEntityReason, PainboxReason}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.types.{ExoSuitType, ImplantType}

trait VitalsActivity {
  def time: Long
}

trait HealingActivity extends VitalsActivity {
  val time: Long = System.currentTimeMillis()
}

trait DamagingActivity extends VitalsActivity {
  def time: Long = data.interaction.hitTime
  def data: DamageResult
}

final case class HealFromKit(kit_def: KitDefinition, amount: Int)
  extends HealingActivity

final case class HealFromEquipment(
    user: PlayerSource,
    equipment_def: EquipmentDefinition,
    amount: Int
) extends HealingActivity

final case class HealFromTerm(term_def: TerminalDefinition, health: Int, armor: Int)
  extends HealingActivity

final case class HealFromImplant(implant: ImplantType, health: Int)
  extends HealingActivity

final case class HealFromExoSuitChange(exosuit: ExoSuitType.Value)
  extends HealingActivity

final case class RepairFromKit(kit_def: KitDefinition, amount: Int)
    extends HealingActivity()

final case class RepairFromEquipment(
    user: PlayerSource,
    equipment_def: EquipmentDefinition,
    amount: Int
) extends HealingActivity

final case class RepairFromTerm(term_def: TerminalDefinition, amount: Int)
    extends HealingActivity

final case class RepairFromArmorSiphon(siphon_def: ToolDefinition, amount: Int)
  extends HealingActivity

final case class VehicleShieldCharge(amount: Int)
  extends HealingActivity //TODO facility

final case class DamageFrom(data: DamageResult)
  extends DamagingActivity

final case class DamageFromProjectile(data: DamageResult)
  extends DamagingActivity

final case class DamageFromPainbox(data: DamageResult)
  extends DamagingActivity

final case class DamageFromEnvironment(data: DamageResult)
  extends DamagingActivity

final case class PlayerSuicide()
  extends DamagingActivity {
  def data: DamageResult = null //TODO do something
}

final case class DamageFromExplodingEntity(data: DamageResult)
  extends DamagingActivity

/**
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * A history of the previous changes in vital statistics of the underlying object is recorded
  * in reverse chronological order.
  */
trait VitalsHistory {

  /** a reverse-order list of chronological events that have occurred to these vital statistics */
  private var vitalsHistory: List[VitalsActivity] = List.empty[VitalsActivity]

  private var lastDamage: Option[DamageResult] = None

  def History: List[VitalsActivity] = vitalsHistory

  /**
    * A `VitalsActivity` event must be recorded.
    * Add new entry to the front of the list (for recent activity).
    * @param action the fully-informed entry
    * @return the list of previous changes to this object's vital statistics
    */
  def History(action: VitalsActivity): List[VitalsActivity] = History(Some(action))

  /**
    * A `VitalsActivity` event must be recorded.
    * Add new entry to the front of the list (for recent activity).
    * @param action the fully-informed entry
    * @return the list of previous changes to this object's vital statistics
    */
  def History(action: Option[VitalsActivity]): List[VitalsActivity] = {
    action match {
      case Some(act) =>
        vitalsHistory = act +: vitalsHistory
      case None => ;
    }
    vitalsHistory
  }

  /**
    * Very common example of a `VitalsActivity` event involving damage.
    * @param result the fully-informed entry
    * @return the list of previous changes to this object's vital statistics
    */
  def History(result: DamageResult): List[VitalsActivity] = {
    result.interaction.cause match {
      case _: ProjectileReason =>
        vitalsHistory = DamageFromProjectile(result) +: vitalsHistory
        lastDamage = Some(result)
      case _: ExplodingEntityReason =>
        vitalsHistory = DamageFromExplodingEntity(result) +: vitalsHistory
        lastDamage = Some(result)
      case _: PainboxReason =>
        vitalsHistory = DamageFromPainbox(result) +: vitalsHistory
      case _: EnvironmentReason =>
        vitalsHistory = DamageFromEnvironment(result) +: vitalsHistory
      case _ => ;
        vitalsHistory = DamageFrom(result) +: vitalsHistory
        if(result.adversarial.nonEmpty) {
          lastDamage = Some(result)
        }
    }
    vitalsHistory
  }

  def LastDamage: Option[DamageResult] = lastDamage

  /**
    * Find, specifically, the last instance of a weapon discharge vital statistics change.
    * @return information about the discharge
    */
  def LastShot: Option[DamageResult] = {
    vitalsHistory.find({ p => p.isInstanceOf[DamageFromProjectile] }) match {
      case Some(entry: DamageFromProjectile) =>
        Some(entry.data)
      case _ =>
        None
    }
  }

  def ClearHistory(): List[VitalsActivity] = {
    val out = vitalsHistory
    vitalsHistory = List.empty[VitalsActivity]
    out
  }
}

//deprecated overrides
object HealFromKit {
  def apply(Target: PlayerSource, amount: Int, kit_def: KitDefinition): HealFromKit =
    HealFromKit(kit_def, amount)
}

object HealFromEquipment {
  def apply(
             Target: PlayerSource,
             user: PlayerSource,
             amount: Int,
             equipment_def: EquipmentDefinition
           ): HealFromEquipment =
    HealFromEquipment(user, equipment_def, amount)
}

object HealFromTerm {
  def apply(Target: PlayerSource, health: Int, armor: Int, term_def: TerminalDefinition): HealFromTerm =
    HealFromTerm(term_def, health, armor)
}

object HealFromImplant {
  def apply(Target: PlayerSource, amount: Int, implant: ImplantType): HealFromImplant =
    HealFromImplant(implant, amount)
}

object HealFromExoSuitChange {
  def apply(Target: PlayerSource, exosuit: ExoSuitType.Value): HealFromExoSuitChange =
    HealFromExoSuitChange(exosuit)
}

object RepairFromKit {
  def apply(Target: PlayerSource, amount: Int, kit_def: KitDefinition): RepairFromKit =
    RepairFromKit(kit_def, amount)
}

object RepairFromEquipment {
  def apply(
             Target: PlayerSource,
             user: PlayerSource,
             amount: Int,
             equipment_def: EquipmentDefinition
           ) : RepairFromEquipment =
    RepairFromEquipment(user, equipment_def, amount)
}

object RepairFromTerm {
  def apply(Target: VehicleSource, amount: Int, term_def: TerminalDefinition): RepairFromTerm =
    RepairFromTerm(term_def, amount)
}

object VehicleShieldCharge {
  def apply(Target: VehicleSource, amount: Int): VehicleShieldCharge =
    VehicleShieldCharge(amount)
}
