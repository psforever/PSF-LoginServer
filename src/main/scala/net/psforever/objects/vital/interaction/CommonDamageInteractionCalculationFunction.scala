// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.interaction

import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.resolution.ResolutionCalculations

/**
  * Functions that are intended to progress
  * processing of a given damage interaction entry object
  * into the damage application fucntion literal.
  */
trait CommonDamageInteractionCalculationFunction {
  def calculate(data: DamageInteraction): ResolutionCalculations.Output

  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output
}
