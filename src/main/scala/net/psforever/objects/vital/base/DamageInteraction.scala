// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.ballistics.{AggravatedDamage, SourceEntry}
import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.vital.resolution.ResolutionCalculations
import net.psforever.types.Vector3

trait DamageInteraction
  extends DamageResult
  with CommonDamageInteractionCalculationFunction {
  def interaction: DamageInteraction = this

  def hitTime: Long

  def target: SourceEntry

  def cause: DamageReason

  def hitPos: Vector3

  def resolution: DamageResolution.Value

  def calculate(): ResolutionCalculations.Output = calculate(data = this)

  def calculate(dtype: DamageType.Value): ResolutionCalculations.Output = calculate(data = this, dtype)

  def calculate(data: DamageInteraction): ResolutionCalculations.Output = cause.calculate(data)

  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output = cause.calculate(data, dtype)
}

final case class GenericDamageInteraction(
                                           resolution: DamageResolution.Value,
                                           target: SourceEntry,
                                           cause: DamageReason,
                                           hitPos: Vector3,
                                           hitTime: Long = System.currentTimeMillis()
                                         ) extends DamageInteraction {
  def damageType: DamageType.Value = DamageType.None

  def damageTypes: Set[DamageType.Value] = Set.empty

  def causesJammering: Boolean = false

  def jammering: List[(TargetValidation, Int)] = List.empty

  def causesAggravation: Boolean = false

  def aggravation: Option[AggravatedDamage] = None

  def adversarial: Option[Adversarial] = None
}

final case class ProjectileDamageInteraction(
                                              target: SourceEntry,
                                              cause: ProjectileReason,
                                              hitPos: Vector3,
                                              hitTime: Long = System.currentTimeMillis()
                                            ) extends DamageInteraction {
  def resolution : DamageResolution.Value = cause.resolution

  def damageType: DamageType.Value = cause.projectile.profile.ProjectileDamageType

  def damageTypes: Set[DamageType.Value] = cause.projectile.profile.ProjectileDamageTypes

  def causesJammering: Boolean = cause.projectile.profile.JammerProjectile

  def jammering: List[(TargetValidation, Int)] = if (causesJammering) {
    cause.projectile.profile.JammedEffectDuration.toList
  } else {
    List.empty
  }

  def causesAggravation: Boolean = cause.projectile.profile.Aggravated.isDefined

  def aggravation: Option[AggravatedDamage] = cause.projectile.profile.Aggravated

  def adversarial: Option[Adversarial] = Some(Adversarial(cause.projectile.owner, target, cause.projectile.attribute_to))
}

object DamageInteraction {
  def apply(resolution: DamageResolution.Value, target: SourceEntry, cause: DamageReason, hitPos: Vector3): DamageInteraction = {
    cause match {
      case o: ProjectileReason if o.resolution == resolution =>
        ProjectileDamageInteraction(target, o, hitPos)
      case o: ProjectileReason =>
        ProjectileDamageInteraction(target, ProjectileReason(resolution, o.projectile, o.damageModel), hitPos)
      case _ =>
        GenericDamageInteraction(resolution, target, cause, hitPos)
    }
  }

  def apply(target: SourceEntry, cause: ProjectileReason, hitPos: Vector3): DamageInteraction = {
    ProjectileDamageInteraction(target, cause, hitPos)
  }
}

object ProjectileDamageInteraction {
  def unapply(obj: DamageInteraction): Option[(SourceEntry, ProjectileReason, Vector3, Long)] = {
    obj.cause match {
      case o: ProjectileReason => Some((obj.target, o, obj.hitPos, obj.hitTime))
      case _ => None
    }
  }
}
