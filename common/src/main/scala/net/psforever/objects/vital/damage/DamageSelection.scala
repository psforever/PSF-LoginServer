// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.ballistics.{ProjectileResolution, ResolvedProjectile}
import net.psforever.objects.vital.NoDamage
import net.psforever.objects.vital.projectile.ProjectileCalculations

/**
  * Maintain information about three primary forms of damage calculation
  * and a means to test which calculation is valid in a given situation.
  */
trait DamageSelection {
  final def None : ProjectileCalculations.Form = NoDamage.Calculate

  def Direct : ProjectileCalculations.Form
  def Splash : ProjectileCalculations.Form
  def Lash : ProjectileCalculations.Form

  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = data.resolution match {
    case ProjectileResolution.Hit => Direct
    case ProjectileResolution.Splash => Splash
    case ProjectileResolution.Lash => Lash
    case _ => None
  }

  def apply(res : ProjectileResolution.Value) : ProjectileCalculations.Form = res match {
    case ProjectileResolution.Hit => Direct
    case ProjectileResolution.Splash => Splash
    case ProjectileResolution.Lash => Lash
    case _ => None
  }
}
