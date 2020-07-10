// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.ballistics._
import net.psforever.objects.vital.{DamageType, NoResistance}
import net.psforever.objects.vital.projectile.ProjectileCalculations

/**
  * Maintain information about four primary forms of resistance calculation
  * and a means to test which calculation is valid in a given situation.
  */
trait ResistanceSelection {
  final def None: ProjectileCalculations.Form = NoResistance.Calculate

  def Direct: ProjectileCalculations.Form
  def Splash: ProjectileCalculations.Form
  def Lash: ProjectileCalculations.Form
  def Aggravated: ProjectileCalculations.Form

  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = data.projectile.profile.ProjectileDamageType match {
    case DamageType.Direct =>     Direct
    case DamageType.Splash =>     Splash
    case DamageType.Lash =>       Lash
    case DamageType.Aggravated => Aggravated
    case _ => None
  }

  def apply(res : DamageType.Value) : ProjectileCalculations.Form = res match {
    case DamageType.Direct =>     Direct
    case DamageType.Splash =>     Splash
    case DamageType.Lash =>       Lash
    case DamageType.Aggravated => Aggravated
    case _ => None
  }
}
