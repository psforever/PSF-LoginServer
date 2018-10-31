// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.ballistics._
import net.psforever.objects.vital.NoResistance
import net.psforever.objects.vital.projectile.ProjectileCalculations

/**
  * Maintain information about four primary forms of resistance calculation
  * and a means to test which calculation is valid in a given situation.
  */
trait ResistanceSelection {
  final def None : ProjectileCalculations.Form = NoResistance.Calculate

  def Direct : ProjectileCalculations.Form
  def Splash : ProjectileCalculations.Form
  def Lash : ProjectileCalculations.Form
  def Aggravated : ProjectileCalculations.Form

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
