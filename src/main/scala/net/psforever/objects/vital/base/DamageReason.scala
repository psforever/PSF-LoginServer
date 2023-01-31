// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, ResolutionCalculations}

/**
  * A wrapper for ambiguity of the "damage source" in damage calculations.
  * The base reason does not convey any specific requirements in regards to the interaction being described.
  */
trait DamageReason {
  /**
    * An indication about how the damage was or will be processed.
    */
  def resolution: DamageResolution.Value

  /**
    * A direct connection to the damage information, numbers and properties.
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
    * The functionality that is necessary for interaction of a vital game object with the rest of the hostile game world.
    */
  def damageModel: DamageAndResistance

  /**
    * Modifiers to the raw/modified damage value that are additive in nature.
    * These modifiers use a selector function to extract the damage value from the profile,
    * a process required to acquire the raw damage value, outlined elsewhere.
    * @return a list of modifications to apply (in order)
    */
  def staticModifiers: List[DamageProfile] = Nil

  /**
    * Modifiers to the raw/modified damage value that are multiplicative or provide disjoint modification.
    * @return a list of modifications to apply (in order)
    */
  def unstructuredModifiers: List[DamageModifiers.Mod] = Nil

  /**
    * The person to be blamed for this.
    */
  def adversary: Option[SourceEntry]

  /**
    * Specifics about the method of damage, expected as an object class's unique identifier.
    * @return defaults to 0
    */
  def attribution: Int = 0

  /**
    * Perform the modified damage value and the basic resistance value allocations
    * to be used against a given valid target.
    * @param data the damaging interaction to be evaluated
    * @return an application function that takes a target and returns a result
    */
  def calculate(data: DamageInteraction): ResolutionCalculations.Output = {
    damageModel.calculate(data)
  }

  /**
    * Perform the modified damage value and the basic resistance value allocations
    * to be used against a given valid target.
    * @param data the damaging interaction to be evaluated
    * @param dtype custom damage property for resistance allocation
    * @return an application function that takes a target and returns a result
    */
  def calculate(data: DamageInteraction, dtype: DamageType.Value): ResolutionCalculations.Output = {
    damageModel.calculate(data, dtype)
  }
}
