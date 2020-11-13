// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.vital.{DamageAndResistance, DamageType}
import net.psforever.objects.vital.test.{DamageInteraction, ProjectileReason}
import net.psforever.types.Vector3

final case class Adversarial(attacker: SourceEntry, defender: SourceEntry)

trait DamageResult {
  def damageTypes: Set[DamageType.Value]

  def causesJammering: Boolean

  def jammering: List[(TargetValidation, Int)]

  def causesAggravation: Boolean

  def aggravation: Option[AggravatedDamage]

  def adversarial: Option[Adversarial]
}

/**
  * An encapsulation of a projectile event that records sufficient historical information
  * about the interaction of weapons discharge and a target
  * to the point that the original event might be reconstructed.
  * Reenacting the calculations of this entry should always produce the same values.
  * @param data information about the damage-causing interaction
  */
final case class ResolvedProjectile(data: DamageInteraction) {
  private val projectileCause = data.cause.asInstanceOf[ProjectileReason]

  val hit_time: Long = System.nanoTime

  def resolution: ProjectileResolution.Value = projectileCause.resolution

  def projectile: Projectile = projectileCause.projectile

  def target: SourceEntry = data.target

  def damage_model: DamageAndResistance = projectileCause.damageModel

  def hit_pos: Vector3 = data.hitPos

  /* experimental; see DamageResult */
  def damageTypes: Set[DamageType.Value] = projectileCause.projectile.profile.ProjectileDamageTypes

  def causesJammering: Boolean = projectileCause.projectile.profile.JammerProjectile

  def jammering: List[(TargetValidation, Int)] = if (causesJammering) {
    projectileCause.projectile.profile.JammedEffectDuration.toList
  } else {
    List.empty
  }

  def causesAggravation: Boolean = projectileCause.projectile.profile.Aggravated.isDefined

  def aggravation: Option[AggravatedDamage] = projectileCause.projectile.profile.Aggravated

  def adversarial: Option[Adversarial] = Some(Adversarial(projectileCause.projectile.owner, data.target))
}

object ResolvedProjectile {
  /**
    * An encapsulation of a projectile event that records sufficient historical information
    * about the interaction of weapons discharge and a target
    * to the point that the original event might be reconstructed.
    * Reenacting the calculations of this entry should always produce the same values.
    * @param projectile the original projectile
    * @param target what the projectile hit
    * @param damage_model the kind of damage model to which the `target` is/was subject
    * @param hit_pos where the projectile hit
    */
  def apply(
             resolution : ProjectileResolution.Value,
             projectile: Projectile,
             target: SourceEntry,
             damage_model: DamageAndResistance,
             hit_pos: Vector3
           ): ResolvedProjectile = {
    ResolvedProjectile(DamageInteraction(target, ProjectileReason(resolution, projectile, damage_model), hit_pos))
  }

    def unapply(
                 obj: ResolvedProjectile
               ) : Option[(ProjectileResolution.Value, Projectile, SourceEntry, DamageAndResistance, Vector3)] =
      obj.data.cause match {
        case ProjectileReason(_resolution, _projectile, damageModel) =>
          Some((_resolution, _projectile, obj.data.target, damageModel, obj.data.hitPos))
        case _ =>
          None
      }
}
