// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.vital.NoResistance
import net.psforever.objects.vital.projectile.ProjectileCalculations
import net.psforever.objects.vital.base.{DamageResult, DamageType, ProjectileDamageInteraction}

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

  def apply(data: DamageResult) : ProjectileCalculations.Form = {
    data match {
      case o : ProjectileDamageInteraction => apply(o)
      case _                               => None
    }
  }

  def apply(data: ProjectileDamageInteraction) : ProjectileCalculations.Form = data.cause.projectile.profile.ProjectileDamageType match {
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
