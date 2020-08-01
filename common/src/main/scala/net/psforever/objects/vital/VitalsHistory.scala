// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile, SourceEntry, VehicleSource}
import net.psforever.objects.definition.{EquipmentDefinition, KitDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.terminals.TerminalDefinition
import net.psforever.types.{ExoSuitType, ImplantType}

abstract class VitalsActivity(target: SourceEntry) {
  def Target: SourceEntry = target
  val t: Long             = System.nanoTime //???

  def time: Long = t
}

abstract class HealingActivity(target: SourceEntry) extends VitalsActivity(target)

abstract class DamagingActivity(target: SourceEntry) extends VitalsActivity(target)

final case class HealFromKit(target: PlayerSource, amount: Int, kit_def: KitDefinition) extends HealingActivity(target)

final case class HealFromEquipment(
    target: PlayerSource,
    user: PlayerSource,
    amount: Int,
    equipment_def: EquipmentDefinition
) extends HealingActivity(target)

final case class HealFromTerm(target: PlayerSource, health: Int, armor: Int, term_def: TerminalDefinition)
    extends HealingActivity(target)

final case class HealFromImplant(target: PlayerSource, amount: Int, implant: ImplantType)
    extends HealingActivity(target)

final case class HealFromExoSuitChange(target: PlayerSource, exosuit: ExoSuitType.Value) extends HealingActivity(target)

final case class RepairFromKit(target: PlayerSource, amount: Int, kit_def: KitDefinition)
    extends HealingActivity(target)

final case class RepairFromEquipment(
    target: PlayerSource,
    user: PlayerSource,
    amount: Int,
    equipment_def: EquipmentDefinition
) extends HealingActivity(target)

final case class RepairFromTerm(target: VehicleSource, amount: Int, term_def: TerminalDefinition)
    extends HealingActivity(target)

final case class VehicleShieldCharge(target: VehicleSource, amount: Int) extends HealingActivity(target) //TODO facility

final case class DamageFromProjectile(data: ResolvedProjectile) extends DamagingActivity(data.target)

final case class DamageFromPainbox(target: PlayerSource, painbox: Painbox, damage: Int) extends DamagingActivity(target)

final case class PlayerSuicide(target: PlayerSource) extends DamagingActivity(target)

final case class DamageFromExplosion(target: PlayerSource, cause: ObjectDefinition) extends DamagingActivity(target)

/**
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * A history of the previous changes in vital statistics of the underlying object is recorded
  * in reverse chronological order.
  */
trait VitalsHistory {

  /** a reverse-order list of chronological events that have occurred to these vital statistics */
  private var vitalsHistory: List[VitalsActivity] = List.empty[VitalsActivity]

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
    * Very common example of a `VitalsActivity` event involving weapon discharge.
    * @param projectile the fully-informed entry of discharge of a weapon
    * @return the list of previous changes to this object's vital statistics
    */
  def History(projectile: ResolvedProjectile): List[VitalsActivity] = {
    vitalsHistory = DamageFromProjectile(projectile) +: vitalsHistory
    vitalsHistory
  }

  /**
    * Find, specifically, the last instance of a weapon discharge vital statistics change.
    * @return information about the discharge
    */
  def LastShot: Option[ResolvedProjectile] = {
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
