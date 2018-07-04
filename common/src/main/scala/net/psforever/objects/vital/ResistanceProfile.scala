// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

trait ResistanceProfile {
  def ResistanceDirectHit : Int

  def ResistanceSplash : Int

  def ResistanceAggravated : Int

  def RadiationShielding : Float

  def Resist(dtype : DamageType.Value) : Float = {
    dtype match {
      case DamageType.Direct => ResistanceDirectHit
      case DamageType.Splash => ResistanceSplash
      case DamageType.Aggravated => ResistanceAggravated
      case DamageType.Radiation => RadiationShielding
      case _ => 0
    }
  }
}

trait ResistanceProfileMutators extends ResistanceProfile {
  def ResistanceDirectHit_=(resist : Int) : Int

  def ResistanceSplash_=(resist : Int) : Int

  def ResistanceAggravated_=(resist : Int) : Int

  def RadiationShielding_=(resist : Float) : Float
}
