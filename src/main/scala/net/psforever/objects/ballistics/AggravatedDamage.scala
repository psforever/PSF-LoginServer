// Copyright (c) 2020 PSForever
package net.psforever.objects.ballistics
import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.serverobject.aura.Aura
import net.psforever.objects.vital.DamageType

/**
  * In what manner of pacing the aggravated damage ticks are applied.
  * @param duration for how long the over-all effect is applied
  * @param ticks a custom number of damage applications,
  *              as opposed to whatever calculations normally estimate the number of applications
  */
final case class AggravatedTiming(duration: Long, ticks: Option[Int])

object AggravatedTiming {
  /**
    * Overloaded constructor that only defines the duration.
    * @param duration for how long the over-all effect lasts
    * @return an `AggravatedTiming` object
    */
  def apply(duration: Long): AggravatedTiming = AggravatedTiming(duration, None)

  /**
    * Overloaded constructor.
    * @param duration for how long the over-all effect lasts
    * @param ticks a custom number of damage applications
    * @return an `AggravatedTiming` object
    */
  def apply(duration: Long, ticks: Int): AggravatedTiming = AggravatedTiming(duration, Some(ticks))
}

/**
  * Aggravation damage has components that are mainly divided by the `DamageType` they inflict.
  * Only `Direct` and `Splash` are valid damage types, however.
  * @param damage_type the type of damage
  * @param degradation_percentage by how much the damage is degraded
  * @param infliction_rate how often the damage is inflicted (ms)
  */
final case class AggravatedInfo(damage_type: DamageType.Value,
                                degradation_percentage: Float,
                                infliction_rate: Long) {
  assert(damage_type == DamageType.Direct || damage_type == DamageType.Splash, s"aggravated damage is an unsupported type - $damage_type")
}

/**
  * Information related to the aggravated damage.
  * @param info the specific kinds of aggravation damage available
  * @param effect_type what effect is exhibited by this aggravated damage
  * @param timing the timing for the damage application
  * @param max_factor na (if the target is a mechanized assault exo-suit?)
  * @param cumulative_damage_degrade na (can multiple instances of this type of aggravated damage apply to the same target at once?)
  * @param vanu_aggravated na (search me)
  * @param targets validation information indicating whether a certain entity is applicable for aggravation
  */
final case class AggravatedDamage(info: List[AggravatedInfo],
                                  effect_type: Aura,
                                  timing: AggravatedTiming,
                                  max_factor: Float,
                                  cumulative_damage_degrade: Boolean,
                                  vanu_aggravated: Boolean,
                                  targets: List[TargetValidation])

object AggravatedDamage {
  /**
    * Overloaded constructor.
    * @param info the specific kinds of aggravation damage available
    * @param effect_type what effect is exhibited by this aggravated damage
    * @param timing the timing for the damage application
    * @param max_factor na
    * @param targets validation information indicating whether a certain entity is applicable for aggravation
    */
  def apply(info: AggravatedInfo,
            effect_type: Aura,
            timing: AggravatedTiming,
            max_factor: Float,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      timing,
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated = false,
      targets
    )

  /**
    * Overloaded constructor.
    * @param info the specific kinds of aggravation damage available
    * @param effect_type what effect is exhibited by this aggravated damage
    * @param timing the timing for the damage application
    * @param max_factor na
    * @param vanu_aggravated na
    * @param targets validation information indicating whether a certain entity is applicable for aggravation
    */
  def apply(info: AggravatedInfo,
            effect_type: Aura,
            timing: AggravatedTiming,
            max_factor: Float,
            vanu_aggravated: Boolean,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      timing,
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated,
      targets
    )

  /**
    * Overloaded constructor.
    * @param info the specific kinds of aggravation damage available
    * @param effect_type what effect is exhibited by this aggravated damage
    * @param duration for how long the over-all effect is applied
    * @param max_factor na
    * @param targets validation information indicating whether a certain entity is applicable for aggravation
    */
  def apply(info: AggravatedInfo,
            effect_type: Aura,
            duration: Long,
            max_factor: Float,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      AggravatedTiming(duration),
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated = false,
      targets
    )

  /**
    * Overloaded constructor.
    * @param info the specific kinds of aggravation damage available
    * @param effect_type what effect is exhibited by this aggravated damage
    * @param duration for how long the over-all effect is applied
    * @param max_factor na
    * @param vanu_aggravated na
    * @param targets validation information indicating whether a certain entity is applicable for aggravation
    */
  def apply(info: AggravatedInfo,
            effect_type: Aura,
            duration: Long,
            max_factor: Float,
            vanu_aggravated: Boolean,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      AggravatedTiming(duration),
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated,
      targets
    )

  def burning(resolution: ProjectileResolution.Value): ProjectileResolution.Value = {
    resolution match {
      case ProjectileResolution.AggravatedDirect => ProjectileResolution.AggravatedDirectBurn
      case ProjectileResolution.AggravatedSplash => ProjectileResolution.AggravatedSplashBurn
      case _ => resolution
    }
  }

  def basicDamageType(resolution: ProjectileResolution.Value): DamageType.Value = {
    resolution match {
      case ProjectileResolution.AggravatedDirect | ProjectileResolution.AggravatedDirectBurn =>
        DamageType.Direct
      case ProjectileResolution.AggravatedSplash | ProjectileResolution.AggravatedSplashBurn =>
        DamageType.Splash
      case _ =>
        DamageType.None
    }
  }
}
