// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.ballistics._
import net.psforever.objects.vital.DamageType
import net.psforever.types.{ExoSuitType, Vector3}

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

  case object MaxDistanceCutoff extends Mod {
    def Calculate: DamageModifiers.Format = function

    private def function(damage: Int, data: ResolvedProjectile): Int = {
      val projectile = data.projectile
      val profile    = projectile.profile
      val distance   = Vector3.Distance(data.hit_pos, projectile.shot_origin)
      if (distance <= profile.DistanceMax) {
        damage
      } else {
        0
      }
    }
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
      val profile    = data.projectile.profile
      val distance   = Vector3.Distance(data.hit_pos, data.target.Position)
      val radius     = profile.DamageRadius
      if (distance <= radius) {
        val base: Float = profile.DamageAtEdge
        (damage * ((1 - base) * ((radius - distance) / radius) + base)).toInt
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

  /*
  Below this point are the calculations for sources of aggravated damage.
  For the most part, these calculations are individualistic and arbitrary.
  They exist in their current form to satisfy observed shots to kill (STK) of specific weapon systems
  according to 2012 standards of the Youtube video series by TheLegendaryNarwhal.
   */
  /**
    * The initial application of aggravated damage against an infantry target
    * where the specific damage component is `Direct`.
    */
  case object InfantryAggravatedDirect extends Mod {
    def Calculate: DamageModifiers.Format =
      BaseAggravatedFormula(ProjectileResolution.AggravatedDirect, DamageType.Direct)
  }

  /**
    * The initial application of aggravated damage against an infantry target
    * where the specific damage component is `Splash`.
    */
  case object InfantryAggravatedSplash extends Mod {
    def Calculate: DamageModifiers.Format =
      BaseAggravatedFormula(ProjectileResolution.AggravatedSplash, DamageType.Splash)
  }

  /**
    * The ongoing application of aggravated damage ticks against an infantry target
    * where the specific damage component is `Direct`.
    * This is called "burning" regardless of what the active aura effect actually is.
    */
  case object InfantryAggravatedDirectBurn extends Mod {
    def Calculate: DamageModifiers.Format =
      BaseAggravatedBurnFormula(ProjectileResolution.AggravatedDirectBurn, DamageType.Direct)
  }

  /**
    * The ongoing application of aggravated damage ticks against an infantry target
    * where the specific damage component is `Splash`.
    * This is called "burning" regardless of what the active aura effect actually is.
    */
  case object InfantryAggravatedSplashBurn extends Mod {
    def Calculate: DamageModifiers.Format =
      BaseAggravatedBurnFormula(ProjectileResolution.AggravatedSplashBurn, DamageType.Splash)
  }

  /**
    * For damage application that involves aggravation of a particular damage type,
    * calculate that initial damage application for infantry targets
    * and produce the modified damage value.
    * Infantry wearing mechanized assault exo-suits (MAX) incorporate an additional modifier.
    * @see `AggravatedDamage`
    * @see `ExoSuitType`
    * @see `InfantryAggravatedDirect`
    * @see `InfantryAggravatedSplash`
    * @see `PlayerSource`
    * @see `ProjectileTarget.AggravatesTarget`
    * @see `ResolvedProjectile`
    * @param resolution the projectile resolution to match against
    * @param damageType the damage type to find in as a component of aggravated information
    * @param damage the base damage value
    * @param data historical information related to the damage interaction
    * @return the modified damage
    */
  private def BaseAggravatedFormula(
                                     resolution: ProjectileResolution.Value,
                                     damageType : DamageType.Value
                                   )
                                   (
                                     damage: Int,
                                     data: ResolvedProjectile
                                   ): Int = {
    if (data.resolution == resolution &&
      data.projectile.quality == ProjectileQuality.AggravatesTarget) {
      (data.projectile.profile.Aggravated, data.target) match {
        case (Some(aggravation), p: PlayerSource) =>
          val aggravatedDamage = aggravation.info.find(_.damage_type == damageType) match {
            case Some(infos) =>
              damage * infos.degradation_percentage + damage
            case _ =>
              damage toFloat
          }
          if(p.ExoSuit == ExoSuitType.MAX) {
            (aggravatedDamage * aggravation.max_factor) toInt
          } else {
            aggravatedDamage toInt
          }
        case _ =>
          damage
      }
    } else {
      damage
    }
  }

  /**
    * For damage application that involves aggravation of a particular damage type,
    * calculate that damage application burn for each tick for infantry targets
    * and produce the modified damage value.
    * Infantry wearing mechanized assault exo-suits (MAX) incorporate an additional modifier.
    * Vanilla infantry incorporate their resistance value into a slightly different calculation than usual.
    * @see `AggravatedDamage`
    * @see `ExoSuitType`
    * @see `InfantryAggravatedDirectBurn`
    * @see `InfantryAggravatedSplashBurn`
    * @see `PlayerSource`
    * @see `ResolvedProjectile`
    * @param resolution the projectile resolution to match against
    * @param damageType the damage type to find in as a component of aggravated information
    * @param damage the base damage value
    * @param data historical information related to the damage interaction
    * @return the modified damage
    */
  private def BaseAggravatedBurnFormula(
                                         resolution: ProjectileResolution.Value,
                                         damageType : DamageType.Value
                                       )
                                       (
                                         damage: Int,
                                         data: ResolvedProjectile
                                       ): Int = {
    if (data.resolution == resolution) {
      (data.projectile.profile.Aggravated, data.target) match {
        case (Some(aggravation), p: PlayerSource) =>
          val degradation = aggravation.info.find(_.damage_type == damageType) match {
            case Some(info) =>
              info.degradation_percentage
            case _ =>
              1f
          }
          if (p.exosuit == ExoSuitType.MAX) {
            (damage * degradation * aggravation.max_factor) toInt
          } else {
            val resist = data.damage_model.ResistUsing(data)(data)
            //add resist to offset resist subtraction later
            if (damage > resist) {
              ((damage - resist) * degradation).toInt + resist
            } else {
              (damage * degradation).toInt + resist
            }
          }
        case _ =>
          0
      }
    } else {
      damage
    }
  }

  /**
    * For damage application that involves aggravation of a fireball (Dragon secondary fire mode),
    * perform 1 damage.
    * @see `ResolvedProjectile`
    */
  case object FireballAggravatedBurn extends Mod {
    def Calculate: DamageModifiers.Format = formula

    private def formula(damage: Int, data: ResolvedProjectile): Int = {
      if (damage > 0 &&
          data.resolution == ProjectileResolution.AggravatedDirectBurn ||
          data.resolution == ProjectileResolution.AggravatedSplashBurn) {
        //add resist to offset resist subtraction later
        1 + data.damage_model.ResistUsing(data)(data)
      } else {
        damage
      }
    }
  }

  /**
    * The initial application of aggravated damage against an aircraft target.
    * Primarily for use in the starfire weapon system.
    * @see `AggravatedDamage`
    * @see `ProjectileQuality.AggravatesTarget`
    * @see `ResolvedProjectile`
    */
  case object StarfireAggravated extends Mod {
    def Calculate: DamageModifiers.Format = formula

    private def formula(damage: Int, data: ResolvedProjectile): Int = {
      if (data.resolution == ProjectileResolution.AggravatedDirect &&
        data.projectile.quality == ProjectileQuality.AggravatesTarget) {
        data.projectile.profile.Aggravated match {
          case Some(aggravation) =>
            aggravation.info.find(_.damage_type == DamageType.Direct) match {
              case Some(infos) =>
                (damage * infos.degradation_percentage + damage) toInt
              case _ =>
                damage
            }
          case _ =>
            damage
        }
      } else {
        damage
      }
    }
  }

  /**
    * The ongoing application of aggravated damage ticks against an aircraft target.
    * Primarily for use in the starfire weapon system.
    * This is called "burning" regardless of what the active aura effect actually is.
    * @see `AggravatedDamage`
    * @see `ProjectileQuality`
    * @see `ResolvedProjectile`
    */
  case object StarfireAggravatedBurn extends Mod {
    def Calculate: DamageModifiers.Format = formula

    private def formula(damage: Int, data: ResolvedProjectile): Int = {
      if (data.resolution == ProjectileResolution.AggravatedDirectBurn) {
        data.projectile.profile.Aggravated match {
          case Some(aggravation) =>
            aggravation.info.find(_.damage_type == DamageType.Direct) match {
              case Some(infos) =>
                (math.floor(damage * infos.degradation_percentage) * data.projectile.quality.mod) toInt
              case _ =>
                damage
            }
          case _ =>
            0
        }
      } else {
        damage
      }
    }
  }

  /**
    * The initial application of aggravated damage against a target.
    * Primarily for use in the comet weapon system.
    * @see `AggravatedDamage`
    * @see `ProjectileQuality.AggravatesTarget`
    * @see `ResolvedProjectile`
    */
  case object CometAggravated extends Mod {
    def Calculate: DamageModifiers.Format = formula

    private def formula(damage: Int, data: ResolvedProjectile): Int = {
      if (data.resolution == ProjectileResolution.AggravatedDirect &&
        data.projectile.quality == ProjectileQuality.AggravatesTarget) {
        data.projectile.profile.Aggravated match {
          case Some(aggravation) =>
            aggravation.info.find(_.damage_type == DamageType.Direct) match {
              case Some(infos) =>
                damage - (damage * infos.degradation_percentage) toInt
              case _ =>
                damage
            }
          case _ =>
            damage
        }
      } else {
        damage
      }
    }
  }

  /**
    * The ongoing application of aggravated damage ticks against a target.
    * Primarily for use in the comet weapon system.
    * This is called "burning" regardless of what the active aura effect actually is.
    * @see `AggravatedDamage`
    * @see `ProjectileQuality`
    * @see `ResolvedProjectile`
    */
  case object CometAggravatedBurn extends Mod {
    def Calculate: DamageModifiers.Format = formula

    private def formula(damage: Int, data: ResolvedProjectile): Int = {
      if (data.resolution == ProjectileResolution.AggravatedDirectBurn) {
        data.projectile.profile.Aggravated match {
          case Some(aggravation) =>
            aggravation.info.find(_.damage_type == DamageType.Direct) match {
              case Some(infos) =>
                damage - (damage * infos.degradation_percentage) toInt
              case _ =>
                damage
            }
          case _ =>
            0
        }
      } else {
        damage
      }
    }
  }
}
