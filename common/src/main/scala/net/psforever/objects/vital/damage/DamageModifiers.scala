// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.ballistics.{ProjectileResolution, ResolvedProjectile}
import net.psforever.types.Vector3

/**
  * Adjustments performed on the subsequent manipulations of the "base damage" value of an attack vector
  * (like a projectile).<br>
  * <br>
  * Unlike static damage modifications which are structured like other `DamageProfiles`
  * and offer purely additive or subtractive effects on the base damage,
  * these modifiers should focus on unstructured, scaled manipulation of the value.
  * The most common modifiers change the damage value based on distance between two points, called "degrading".
  * The list of modifiers must be allocated in a single attempt, overriding previously-set modifiers.
  * @see `DamageCalculations.DamageWithModifiers`
  * @see `DamageProfile`
  * @see `ResolvedProjectile`
  */
trait DamageModifiers {
  private var mods: List[DamageModifiers.Mod] = Nil

  def Modifiers: List[DamageModifiers.Mod] = mods

  def Modifiers_=(modifier: DamageModifiers.Mod): List[DamageModifiers.Mod] = Modifiers_=(List(modifier))

  def Modifiers_=(modifiers: List[DamageModifiers.Mod]): List[DamageModifiers.Mod] = {
    mods = modifiers
    Modifiers
  }
}

object DamageModifiers {
  type Format = (Int, ResolvedProjectile) => Int

  trait Mod {
    /** Perform the underlying calculations, returning a modified value from the input value. */
    def Calculate: DamageModifiers.Format
  }

  /** The input value is the same as the output value. */
  case object SameHit extends Mod {
    def Calculate: DamageModifiers.Format = function

    private def function(damage: Int, data: ResolvedProjectile): Int = damage
  }

  /**
    * The input value degrades (lessens)
    * the further the distance between the point of origin (`shot_origin`)
    * and the point of encounter (`hit_pos`) of its vector (projectile).
    * If the value is not set to degrade over any distance within its maximum distance, the value goes unmodified.
    * If the value is encountered beyond its maximum distance, the value is zero'd.
    */
  case object DistanceDegrade extends Mod {
    def Calculate: DamageModifiers.Format = function

    private def function(damage: Int, data: ResolvedProjectile): Int = {
      val projectile = data.projectile
      val profile    = projectile.profile
      val distance   = Vector3.Distance(data.hit_pos, projectile.shot_origin)
      if (distance <= profile.DistanceMax) {
        if (profile.DistanceNoDegrade == profile.DistanceMax || distance <= profile.DistanceNoDegrade) {
          damage
        } else {
          damage - ((damage - profile.DegradeMultiplier * damage) * ((distance - profile.DistanceNoDegrade) / (profile.DistanceMax - profile.DistanceNoDegrade))).toInt
        }
      } else {
        0
      }
    }
  }

  /**
    * The input value degrades (lessens)
    * the further the distance between the point of origin (target position)
    * and the point of encounter (`hit_pos`) of its vector (projectile).
    * If the value is encountered beyond its maximum radial distance, the value is zero'd.
    */
  case object RadialDegrade extends Mod {
    def Calculate: DamageModifiers.Format = function

    private def function(damage: Int, data: ResolvedProjectile): Int = {
      val projectile = data.projectile
      val profile    = projectile.profile
      val distance   = Vector3.Distance(data.hit_pos, data.target.Position)
      val radius     = profile.DamageRadius
      if (distance <= radius) {
        val base: Float    = profile.DamageAtEdge
        val degrade: Float = (1 - base) * ((radius - distance) / radius) + base
        (damage * degrade).toInt
      } else {
        0
      }
    }
  }

  /**
    * Lashing is the property of a projectile affecting nearby targets without coming into direct contact with them.
    * The effect only activates after 5m from the point of origin (`shot_origin`) before the maximum distance.
    * If lashing does not apply, the value goes unmodified.
    * If lashing is valid but the value is encountered beyond its maximum radial distance, the value is zero'd.
    */
  case object Lash extends Mod {
    def Calculate: DamageModifiers.Format = function

    private def function(damage: Int, data: ResolvedProjectile): Int = {
      if (data.resolution == ProjectileResolution.Lash) {
        val distance = Vector3.Distance(data.hit_pos, data.projectile.shot_origin)
        if (distance > 5 && distance <= data.projectile.profile.DistanceMax) {
          (damage * 0.2f) toInt
        } else {
          0
        }
      } else {
        damage
      }
    }
  }
}
