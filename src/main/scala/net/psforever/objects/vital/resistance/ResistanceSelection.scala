// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.vital.NoResistance
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}

/**
  * Maintain information about four primary forms of resistance calculation
  * and a means to test which calculation is valid in a given situation.
  */
trait ResistanceSelection {
  def Direct: ResistanceSelection.Format
  def Splash: ResistanceSelection.Format
  def Lash: ResistanceSelection.Format
  def Aggravated: ResistanceSelection.Format

  def apply(data: DamageResult) : ResistanceSelection.Format = data.damageType match {
      case DamageType.Direct     => Direct
      case DamageType.Splash     => Splash
      case DamageType.Lash       => Lash
      case DamageType.Aggravated => Aggravated
      case _                     => ResistanceSelection.None
    }

  def apply(res: DamageType.Value) : ResistanceSelection.Format = res match {
    case DamageType.Direct     => Direct
    case DamageType.Splash     => Splash
    case DamageType.Lash       => Lash
    case DamageType.Aggravated => Aggravated
    case _                     => ResistanceSelection.None
  }
}

object ResistanceSelection {
  type Format = DamageInteraction => Int

  final val None: ResistanceSelection.Format = NoResistance.Calculate
}
