// Copyright (c) 2020 PSForever
package net.psforever.objects.ballistics
import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.serverobject.aggravated.Aura
import net.psforever.objects.vital.DamageType

final case class AggravatedInfo(damage_type: DamageType.Value,
                                degradation_percentage: Float,
                                infliction_rate: Long) {
  assert(damage_type == DamageType.Direct || damage_type == DamageType.Splash, s"aggravated damage is an unsupported type - $damage_type")
}

final case class AggravatedDamage(info: List[AggravatedInfo],
                                  effect_type: Aura,
                                  duration: Long,
                                  max_factor: Float,
                                  cumulative_damage_degrade: Boolean,
                                  vanu_aggravated: Boolean,
                                  targets: List[TargetValidation])

object AggravatedDamage {
  def apply(info: AggravatedInfo,
            effect_type: Aura,
            duration: Long,
            max_factor: Float,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      duration,
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated = false,
      targets
    )

  def apply(info: AggravatedInfo,
            effect_type: Aura,
            duration: Long,
            max_factor: Float,
            vanu_aggravated: Boolean,
            targets: List[TargetValidation]): AggravatedDamage =
    AggravatedDamage(
      List(info),
      effect_type,
      duration,
      max_factor,
      cumulative_damage_degrade = true,
      vanu_aggravated,
      targets
    )
}
