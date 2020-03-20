// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.vital.resolution.ResolutionCalculations

/**
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * The amount of HDHR is controlled by the damage model of this vital object reacting to stimulus.
  * The damage model is provided.
  */
trait Vitality extends VitalsHistory {
  private var health : Int = Definition.DefaultHealth
  private var defaultHealth : Option[Int] = None
  private var maxHealth : Option[Int] = None

  def Health : Int = health

  def Health_=(assignHealth : Int) : Int = {
    health = math.min(math.max(0, assignHealth), MaxHealth)
    Health
  }

  def DefaultHealth : Int = defaultHealth.getOrElse(Definition.DefaultHealth)

  def MaxHealth : Int = maxHealth.getOrElse(Definition.MaxHealth)

  def MaxHealth_=(default : Int) : Int = MaxHealth_=(Some(default))

  def MaxHealth_=(default : Option[Int]) : Int = {
    maxHealth = default
    MaxHealth
  }

  def CanDamage : Boolean = {
    Definition.Damageable && Health > 0
  }

  def CanRepair : Boolean = {
    Definition.Repairable && Health < MaxHealth && (Health > 0 || Definition.RepairIfDestroyed)
  }

  def DamageModel : DamageResistanceModel

  def Definition : VitalityDefinition
}

object Vitality {

  /**
    * Provide the damage model-generated functionality
    * that would properly enact the calculated changes of a vital statistics event
    * upon a given vital object.
    * @param func a function literal
    */
  final case class Damage(func : ResolutionCalculations.Output)

  final case class DamageOn(obj : Vitality, func : ResolutionCalculations.Output)

  /**
    * Report that a vitals object must be updated due to damage.
    * @param obj the vital object
    */
  final case class DamageResolution(obj : Vitality, cause : ResolvedProjectile)
}
