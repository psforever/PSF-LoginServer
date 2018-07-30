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
abstract class DamageResistCalculations[A](calcFunc : (ResolvedProjectile)=>((Int, Int)=>A),
                                           applyFunc : (A, ResolvedProjectile)=>ResolutionCalculations.Output)
  extends ResolutionCalculations {
  def Calculate(damages : ProjectileCalculations.Form, resistances : ProjectileCalculations.Form, data : ResolvedProjectile) : ResolutionCalculations.Output = {
    val dam : Int = damages(data)
    val res : Int = resistances(data)
    val mod = calcFunc(data)
    val modDam = mod(dam, res)
    applyFunc(modDam, data)
  }
}
