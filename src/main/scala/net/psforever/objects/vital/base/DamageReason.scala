// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.{CommonDamageInteractionCalculationFunction, DamageInteraction}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.ResolutionCalculations

/**
  * A wrapper for ambiguity of the "damage source" in damage calculations.
  * The base reason does not convey any specific requirements in regards to the interaction being described.
  */
trait DamageReason
  extends CommonDamageInteractionCalculationFunction {
  /**
    * A direct connection to the damage infomration, numbers and properties.
    */
  def source: DamageProperties

  /**
    * Determine whether two damage sources are equivalent.
    * @param test the damage source to compare against
    * @return `true`, if equivalent;
    *        `false`, otherwise
    */
  def same(test: DamageReason): Boolean

  /**
    * Modifiers to the raw/modified damage value that are additive in nature.
    * These modifiers use a selector function to extract the damage value from the profile,
    * a process required to acquire the raw damage value, outlined elsewhere.
    * @return a list of modifications to apply (in order)
    */
  def staticModifiers: List[DamageProfile] = Nil

  /**
    * Modifiers to the raw/modified damage value that are sclara or provide disjoint modification.
    * @return a list of modifications to apply (in order)
    */
  def unstructuredModifiers: List[DamageModifiers.Mod] = Nil

  def calculate(data: DamageInteraction): ResolutionCalculations.Output = (_: Any) => data

  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output = (_: Any) => data
}
