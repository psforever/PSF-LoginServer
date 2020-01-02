// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resolution

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.vital.projectile.ProjectileCalculations

/**
  * A specific implementation of `ResolutionCalculations` that deals with
  * the damage value and the resistance value in a specific manner.
  * (The input type of the function literal output of `calcFunc`.)
  * @param calcFunc a function literal that retrieves the function
  *                 that factors the affects of damage and resistance values
  * @param applyFunc a function literal that applies the final modified values to a target object
  * @tparam A an internal type that converts between `calcFunc`'s output and `applyFunc`'s input;
  *           never has to be defined explicitly, but will be checked upon object definition
  */
abstract class DamageResistCalculations[A](calcFunc : ResolvedProjectile=>(Int, Int)=>A,
                                           applyFunc : (A, ResolvedProjectile)=>ResolutionCalculations.Output)
  extends ResolutionCalculations {
  def Calculate(damages : ProjectileCalculations.Form, resistances : ProjectileCalculations.Form, data : ResolvedProjectile) : ResolutionCalculations.Output = {
    val modDam = Sample(damages, resistances, data)
    applyFunc(modDam, data)
  }

  /**
    * An intermediate step of the normal `Calculate` operation that retrieves the damage values in their transitory form.
    * @param damages the function that calculations raw damage values
    * @param resistances the function that calculates resistance values
    * @param data a historical projectile interaction;
    *             the origin of the data used to extract damage and resistance values
    * @return the transitory form of the modified damage(s);
    *         usually, a single `Int` value or a tuple of `Int` values
    */
  def Sample(damages : ProjectileCalculations.Form, resistances : ProjectileCalculations.Form, data : ResolvedProjectile) : A = {
    val dam : Int = damages(data)
    val res : Int = resistances(data)
    val mod = calcFunc(data)
    mod(dam, res)
  }
}
