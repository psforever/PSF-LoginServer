// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.ballistics.{AggravatedDamage, SourceEntry}
import net.psforever.objects.equipment.TargetValidation
import net.psforever.types.Vector3

trait DamageInteraction extends DamageResult {
  def hitTime: Long

  def target: SourceEntry

  def cause: DamageReason

  def hitPos: Vector3

  def calculate(): Any => DamageResult = calculate(data = this)

  def calculate(data: DamageInteraction): Any => DamageResult = cause.calculate(data)
}

final case class GenericDamageInteraction(
                                           target: SourceEntry,
                                           cause: DamageReason,
                                           hitPos: Vector3,
                                           hitTime: Long = System.currentTimeMillis()
                                         ) extends DamageInteraction {
  def interaction = this

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
  def interaction = this

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
  def apply(target: SourceEntry, cause: DamageReason, hitPos: Vector3): DamageInteraction = {
    cause match {
      case o: ProjectileReason => ProjectileDamageInteraction(target, o, hitPos)
      case _ => GenericDamageInteraction(target, cause, hitPos)
    }
  }
}

object ProjectileDamageInteraction {
  def unapply(obj: DamageInteraction): Option[ProjectileDamageInteraction] = {
    obj.cause match {
      case o: ProjectileReason => Some(ProjectileDamageInteraction(obj.target, o, obj.hitPos, obj.hitTime))
      case _ => None
    }
  }
}
