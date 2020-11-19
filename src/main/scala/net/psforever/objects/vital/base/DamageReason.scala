// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.{CommonDamageInteractionCalculationFunction, DamageInteraction}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.ResolutionCalculations

trait DamageReason
  extends CommonDamageInteractionCalculationFunction {
  def source: DamageProperties

  def same(test: DamageReason): Boolean

  def staticModifiers: List[DamageProfile] = Nil

  def unstructuredModifiers: List[DamageModifiers.Mod] = Nil

  def calculate(data: DamageInteraction): ResolutionCalculations.Output = (_: Any) => data

  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output = (_: Any) => data
}
