// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.projectile

import net.psforever.objects.ballistics._
import net.psforever.objects.equipment.ChargeFireModeDefinition
import net.psforever.objects.vital.base._
import net.psforever.objects.vital.damage.DamageModifierFunctions
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.{ExoSuitType, Vector3}

/** If the calculated distance is greater than the maximum distance of the projectile, damage is zero'd. */
case object MaxDistanceCutoff extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    val projectile = cause.projectile
    val profile    = projectile.profile
    val distance   = Vector3.Distance(data.hitPos, projectile.shot_origin)
    if (distance <= profile.DistanceMax) {
      damage
    } else {
      0
    }
  }
}

/** If the calculated distance is greater than a custom distance, damage is zero'd. */
case class CustomDistanceCutoff(cutoff: Float) extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    val projectile = cause.projectile
    val distance   = Vector3.Distance(data.hitPos, projectile.shot_origin)
    if (distance <= cutoff) {
      damage
    } else {
      0
    }
  }
}

/**
  * The input value degrades (lessens)
  * the further the distance between the point of origin (`shot_origin`)
  * and the point of encounter (`hitPos`) of its vector (projectile).
  * If the value is not set to degrade over any distance within its maximum distance, the value goes unmodified.
  * If the value is encountered beyond its maximum distance, the value is zero'd.
  */
case object DistanceDegrade extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int =
    ProjectileDamageModifierFunctions.distanceDegradeFunction(damage, data, cause)
}

/**
  * Lashing is the property of a projectile affecting nearby targets without coming into direct contact with them.
  * The effect only activates after 5m from the point of origin (`shot_origin`) before the maximum distance.
  * If lashing does not apply, the value goes unmodified.
  * If lashing is valid but the value is encountered beyond its maximum radial distance, the value is zero'd.
  */
case object Lash extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (cause.resolution == DamageResolution.Lash) {
      val distance = Vector3.Distance(data.hitPos, cause.projectile.shot_origin)
      if (distance > 5 && distance <= cause.projectile.profile.DistanceMax) {
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
Aggravated damage.
For the most part, these calculations are individualistic and arbitrary.
They exist in their current form to satisfy observed shots to kill (STK) of specific weapon systems
according to 2012 standards of the Youtube video series by TheLegendaryNarwhal.
 */
/**
  * The initial application of aggravated damage against an infantry target
  * where the specific damage component is `Direct`.
  */
case object InfantryAggravatedDirect extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int =
    ProjectileDamageModifierFunctions.baseAggravatedFormula(DamageResolution.AggravatedDirect, DamageType.Direct)(damage, data, cause)
}

/**
  * The initial application of aggravated damage against an infantry target
  * where the specific damage component is `Splash`.
  */
case object InfantryAggravatedSplash extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int =
    ProjectileDamageModifierFunctions.baseAggravatedFormula(DamageResolution.AggravatedSplash, DamageType.Splash)(damage, data, cause)
}

/**
  * The ongoing application of aggravated damage ticks against an infantry target
  * where the specific damage component is `Direct`.
  * This is called "burning" regardless of what the active aura effect actually is.
  */
case object InfantryAggravatedDirectBurn extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int =
    ProjectileDamageModifierFunctions.baseAggravatedBurnFormula(DamageResolution.AggravatedDirectBurn, DamageType.Direct)(damage, data, cause)
}

/**
  * The ongoing application of aggravated damage ticks against an infantry target
  * where the specific damage component is `Splash`.
  * This is called "burning" regardless of what the active aura effect actually is.
  */
case object InfantryAggravatedSplashBurn extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int =
    ProjectileDamageModifierFunctions.baseAggravatedBurnFormula(DamageResolution.AggravatedSplashBurn, DamageType.Splash)(damage, data, cause)
}

/**
  * For damage application that involves aggravation of a fireball (Dragon secondary fire mode),
  * perform 1 damage.
  */
case object FireballAggravatedBurn extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (damage > 0 &&
        (data.resolution == DamageResolution.AggravatedDirectBurn ||
         data.resolution == DamageResolution.AggravatedSplashBurn)) {
      //add resist to offset resist subtraction later
      1 + cause.damageModel.ResistUsing(data)(data)
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
  */
case object StarfireAggravated extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (cause.resolution == DamageResolution.AggravatedDirect &&
        cause.projectile.quality == ProjectileQuality.AggravatesTarget) {
      data.cause.source.Aggravated match {
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
  */
case object StarfireAggravatedBurn extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (cause.resolution == DamageResolution.AggravatedDirectBurn) {
      data.cause.source.Aggravated match {
        case Some(aggravation) =>
          aggravation.info.find(_.damage_type == DamageType.Direct) match {
            case Some(infos) =>
              (math.floor(damage * infos.degradation_percentage) * cause.projectile.quality.mod) toInt
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
  */
case object CometAggravated extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (cause.resolution == DamageResolution.AggravatedDirect &&
        cause.projectile.quality == ProjectileQuality.AggravatesTarget) {
      data.cause.source.Aggravated match {
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
  */
case object CometAggravatedBurn extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (cause.resolution == DamageResolution.AggravatedDirectBurn) {
      data.cause.source.Aggravated match {
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

/**
  * If the projectile has charging properties,
  * and the weapon that produced the projectile has charging mechanics,
  * calculate the current value of the damage as a sum
  * of some minimum damage and scaled normal damage.
  * The projectile quality has information about the "factor" of damage scaling.
  * @see `ChargeDamage`
  * @see `ChargeFireModeDefinition`
  * @see `ProjectileQuality`
  */
case object SpikerChargeDamage extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    val projectile = cause.projectile
    (projectile.fire_mode, projectile.profile.Charging) match {
      case (_: ChargeFireModeDefinition, Some(info: ChargeDamage)) =>
        val chargeQuality = math.max(0f, math.min(projectile.quality.mod, 1f))
        cause.damageModel.DamageUsing(info.min) + (damage * chargeQuality).toInt
      case _ =>
        damage
    }
  }
}

/**
  * If the damage is resolved through a `HitDamage` packet,
  * calculate the damage as a function of its degrading value over distance traveled by its carrier projectile.
  * @see `distanceDegradeFunction`
  * @see `ProjectileQuality`
  */
case object FlakHit extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if(cause.resolution == DamageResolution.Hit) {
      ProjectileDamageModifierFunctions.distanceDegradeFunction(damage, data, cause)
    } else {
      damage
    }
  }
}

/**
  * If the damage is resolved through a `SplashHitDamage` packet,
  * calculate the damage as a function of its degrading value over distance
  * between the hit position of the projectile and the position of the target.
  * @see `DamageModifierFunctions.radialDegradeFunction`
  * @see `ProjectileQuality`
  */
case object FlakBurst extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if(cause.resolution == DamageResolution.Splash) {
      DamageModifierFunctions.radialDegradeFunction(damage, data, cause)
    } else {
      damage
    }
  }
}

/**
  * If the damage is resolved by way of a melee weapon,
  * the damage might be increased if the attack was initiated
  * while the attacker was under the effect of an active Melee Boost implant.
  * @see `GlobalDefinitions.melee_booster`
  * @see `ProjectileQuality`
  */
case object MeleeBoosted extends ProjectileDamageModifiers.Mod {
  override def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    cause.projectile.quality.mod.toInt + damage
  }
}

/**
  * If the Flail's projectile exceeds it's distance before degrade in travel distance,
  * the damage caused by the projectile increases by up to multiple times its base damage at 600m.
  * It does not inflate for further beyond 600m.
  */
case object FlailDistanceDamageBoost extends ProjectileDamageModifiers.Mod {
  override def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    val projectile = cause.projectile
    val profile = projectile.profile
    val distance   = Vector3.Distance(data.hitPos.xy, projectile.shot_origin.xy)
    val distanceNoDegrade = profile.DistanceNoDegrade
    val distanceNoMultiplier = 600f - distanceNoDegrade
    if (distance > profile.DistanceMax) {
      0
    } else if (distance >= distanceNoDegrade) {
      damage + (damage * (profile.DegradeMultiplier - 1) *
                math.min(distance - distanceNoDegrade, distanceNoMultiplier) / distanceNoMultiplier).toInt
    } else {
      damage
    }
  }
}

/**
  * If the damge is caused by a projectile that emits a field that permeates armor,
  * determine by how much the traversed armor's shielding reduces the damage.
  * Infantry take damage, reduced only if one is equipped with a mechanized assault exo-suit.
  */
case object ShieldAgainstRadiation extends ProjectileDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    if (data.resolution == DamageResolution.Radiation) {
      data.target match {
        case p: PlayerSource if p.ExoSuit == ExoSuitType.MAX =>
          damage - (damage * p.Modifiers.RadiationShielding).toInt
        case _: PlayerSource =>
          damage
        case _ =>
          0
      }
    } else {
      damage
    }
  }
}

/* Functions */
object ProjectileDamageModifierFunctions {
  /**
    * The input value degrades (lessens)
    * the further the distance between the point of origin (`shot_origin`)
    * and the point of encounter (`hitPos`) of its vector (projectile).
    * If the value is not set to degrade over any distance within its maximum distance, the value goes unmodified.
    * If the value is encountered beyond its maximum distance, the value is zero'd.
    */
  def distanceDegradeFunction(damage: Int, data: DamageInteraction, cause: ProjectileReason): Int = {
    val projectile = cause.projectile
    val profile    = projectile.profile
    val distance   = Vector3.Distance(data.hitPos, projectile.shot_origin)
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
    * @param resolution the projectile resolution to match against
    * @param damageType the damage type to find in as a component of aggravated information
    * @param damage the base damage value
    * @param data historical information related to the damage interaction
    * @return the modified damage
    */
  def baseAggravatedFormula(
                             resolution: DamageResolution.Value,
                             damageType : DamageType.Value
                           )
                           (
                             damage: Int,
                             data: DamageInteraction,
                             cause: ProjectileReason
                           ): Int = {
    if (cause.resolution == resolution &&
        cause.projectile.quality == ProjectileQuality.AggravatesTarget) {
      (data.cause.source.Aggravated, data.target) match {
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
    * @param resolution the projectile resolution to match against
    * @param damageType the damage type to find in as a component of aggravated information
    * @param damage the base damage value
    * @param data historical information related to the damage interaction
    * @return the modified damage
    */
  def baseAggravatedBurnFormula(
                                 resolution: DamageResolution.Value,
                                 damageType : DamageType.Value
                               )
                               (
                                 damage: Int,
                                 data: DamageInteraction,
                                 cause: ProjectileReason
                               ): Int = {
    if (data.resolution == resolution) {
      (data.cause.source.Aggravated, data.target) match {
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
            val resist = cause.damageModel.ResistUsing(data)(data)
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
}
