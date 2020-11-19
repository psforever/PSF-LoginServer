// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.interaction

import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.resolution.ResolutionCalculations

trait CommonDamageInteractionCalculationFunction {
  def calculate(data: DamageInteraction): ResolutionCalculations.Output

  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output
}
