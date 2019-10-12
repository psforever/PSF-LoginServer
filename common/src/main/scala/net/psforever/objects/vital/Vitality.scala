// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.{PlayerSource, ResolvedProjectile, SourceEntry, VehicleSource}
import net.psforever.objects.definition.KitDefinition
import net.psforever.objects.serverobject.terminals.TerminalDefinition
import net.psforever.types.{ExoSuitType, ImplantType}

abstract class VitalsActivity(target : SourceEntry) {
  def Target : SourceEntry = target
  val t : Long = System.nanoTime //???

  def time : Long = t
}

abstract class HealingActivity(target : SourceEntry) extends VitalsActivity(target)

abstract class DamagingActivity(target : SourceEntry) extends VitalsActivity(target)

final case class HealFromKit(target : PlayerSource, amount : Int, kit_def : KitDefinition) extends HealingActivity(target)

final case class HealFromTerm(target : PlayerSource, health : Int, armor : Int, term_def : TerminalDefinition) extends HealingActivity(target)

final case class HealFromImplant(target : PlayerSource, amount : Int, implant : ImplantType.Value) extends HealingActivity(target)

final case class HealFromExoSuitChange(target : PlayerSource, exosuit : ExoSuitType.Value) extends HealingActivity(target)

final case class RepairFromKit(target : PlayerSource, amount : Int, kit_def : KitDefinition) extends HealingActivity(target)

final case class RepairFromTerm(target : VehicleSource, amount : Int, term_def : TerminalDefinition) extends HealingActivity(target)

final case class VehicleShieldCharge(target : VehicleSource, amount : Int) extends HealingActivity(target) //TODO facility

final case class DamageFromProjectile(data : ResolvedProjectile) extends DamagingActivity(data.target)

final case class PlayerSuicide(target : PlayerSource) extends DamagingActivity(target)

/**
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * The amount of HDHR is controlled by the damage model of this vital object reacting to stimulus.
  * A history of the previous changes in vital statistics of the underlying object is recorded
  * in reverse chronological order.
  * The damage model is also provided.
  */
trait Vitality {
  this : PlanetSideGameObject =>

  /** a reverse-order list of chronological events that have occurred to these vital statistics */
  private var vitalHistory : List[VitalsActivity] = List.empty[VitalsActivity]

  def History : List[VitalsActivity] = vitalHistory

  /**
    * A `VitalsActivity` event must be recorded.
    * Add new entry to the front of the list (for recent activity).
    * @param action the fully-informed entry
    * @return the list of previous changes to this object's vital statistics
    */
  def History(action : VitalsActivity) : List[VitalsActivity] = {
    vitalHistory = action +: vitalHistory
    vitalHistory
  }

  /**
    * Very common example of a `VitalsActivity` event involving weapon discharge.
    * @param projectile the fully-informed entry of discharge of a weapon
    * @return the list of previous changes to this object's vital statistics
    */
  def History(projectile : ResolvedProjectile) : List[VitalsActivity] = {
    vitalHistory = DamageFromProjectile(projectile) +: vitalHistory
    vitalHistory
  }

  /**
    * Find, specifically, the last instance of a weapon discharge vital statistics change.
    * @return information about the discharge
    */
  def LastShot : Option[ResolvedProjectile] = {
    vitalHistory.find({p => p.isInstanceOf[DamageFromProjectile]}) match {
      case Some(entry : DamageFromProjectile) =>
        Some(entry.data)
      case _ =>
        None
    }
  }

  def ClearHistory() : List[VitalsActivity] = {
    val out = vitalHistory
    vitalHistory = List.empty[VitalsActivity]
    out
  }

  def DamageModel : DamageResistanceModel
}

object Vitality {

  /**
    * Provide the damage model-generated functionality
    * that would properly enact the calculated changes of a vital statistics event
    * upon a given vital object.
    * @param func a function literal
    */
  final case class Damage(func : (Any)=>Unit)

  final case class DamageOn(obj : Vitality, func : (Any)=>Unit)

  /**
    * Report that a vitals object must be updated due to damage.
    * @param obj the vital object
    */
  final case class DamageResolution(obj : Vitality)
}
