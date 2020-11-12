// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.vital.DamageResistanceModel
import net.psforever.objects.vital.test.{DamageInteraction, DamageReason}
import net.psforever.types.Vector3

/**
  * An encapsulation of a projectile event that records sufficient historical information
  * about the interaction of weapons discharge and a target
  * to the point that the original event might be reconstructed.
  * Reenacting the calculations of this entry should always produce the same values.
  * @param data information about the damage-causing interaction
  */
final case class ResolvedProjectile(data: DamageInteraction) {
  val hit_time: Long = System.nanoTime

  def resolution: ProjectileResolution.Value = data.cause.asInstanceOf[DamageReason.Projectile].resolution

  def projectile: Projectile = data.cause.asInstanceOf[DamageReason.Projectile].projectile

  def target: SourceEntry = data.target

  def damage_model: DamageResistanceModel = data.cause.asInstanceOf[DamageReason.Projectile].damageModel

  def hit_pos: Vector3 = data.hitPos
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
             damage_model: DamageResistanceModel,
             hit_pos: Vector3
           ): ResolvedProjectile = {
    ResolvedProjectile(DamageInteraction(target, DamageReason.Projectile(resolution, projectile, damage_model), hit_pos))
  }

    def unapply(
                 obj: ResolvedProjectile
               ) : Option[(ProjectileResolution.Value, Projectile, SourceEntry, DamageResistanceModel, Vector3)] =
      obj.data.cause match {
        case DamageReason.Projectile(_resolution, _projectile, damageModel) =>
          Some((_resolution, _projectile, obj.data.target, damageModel, obj.hit_pos))
        case _ =>
          None
      }
}
