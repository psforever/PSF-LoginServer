// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.base

import net.psforever.objects.vital.interaction.DamageInteraction

/**
  * Adjustments performed on the subsequent manipulations of the "base damage" value of an attack vector
  * (like a projectile).<br>
  * <br>
  * Unlike static damage modifications which are structured like other `DamageProfiles`
  * and offer purely additive or subtractive effects on the base damage,
  * these modifiers should focus on unstructured, scaled manipulation of the value.
  * The most common modifiers change the damage value based on distance between two points, called "degrading".
  * The list of modifiers must be allocated in a single attempt, overriding previously-set modifiers.
  * @see `DamageCalculations.WithModifiers`
  * @see `DamageModifiers.Mod`
  * @see `DamageProfile`
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
  trait Mod {
    /** Perform the underlying calculations, returning a modified value from the input value. */
    final def calculate(damage : Int, data : DamageInteraction) : Int = {
      calculate(damage, data, data.cause)
    }

    def calculate(damage : Int, data : DamageInteraction, cause : DamageReason) : Int
  }
}
