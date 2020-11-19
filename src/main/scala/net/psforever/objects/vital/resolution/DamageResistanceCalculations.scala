// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resolution

import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.resistance.ResistanceSelection

/**
  * A specific implementation of `ResolutionCalculations` that deals with
  * the damage value and the resistance value in a specific manner.
  * (The input type of the function literal output of `calcFunc`.)
  * @see `DamageCalculations.WithModifiers`
  * @param calcFunc a function literal that retrieves the function
  *                 that factors the affects of damage and resistance values
  * @param applyFunc a function literal that applies the final modified values to a target object
  * @param modifiersFunc a function literal that extracts and modifies a numeric damage value;
  *                      even if no modifiers are to be used, the base damage value needs to be extracted;
  *                      defaults to a function that utilizes all of the available information
  * @tparam A an internal type that converts between `calcFunc`'s output and `applyFunc`'s input;
  *           never has to be defined explicitly, but will be checked upon object definition
  */
abstract class DamageResistanceCalculations[A](
    calcFunc: DamageInteraction => (Int, Int) => A,
    applyFunc: (A, DamageInteraction) => ResolutionCalculations.Output,
    modifiersFunc: (DamageCalculations.Selector, DamageInteraction) => Int = DamageCalculations.WithModifiers
) extends ResolutionCalculations {
  def calculate(
      damages: DamageCalculations.Selector,
      resistances: ResistanceSelection.Format,
      data: DamageInteraction
  ): ResolutionCalculations.Output = {
    val dam = modifiersFunc(damages, data)
    val res = resistances(data)
    val mod = calcFunc(data)
    val modDam = mod(dam, res)
    applyFunc(modDam, data)
  }
}
