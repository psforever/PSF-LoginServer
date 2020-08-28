// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.vital.DamageResistanceModel
import net.psforever.types.Vector3

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
final case class ResolvedProjectile(
    resolution : ProjectileResolution.Value,
    projectile: Projectile,
    target: SourceEntry,
    damage_model: DamageResistanceModel,
    hit_pos: Vector3
) {
  val hit_time: Long = System.nanoTime
}
