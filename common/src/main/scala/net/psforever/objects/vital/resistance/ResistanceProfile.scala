// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.vital.DamageType

/**
  * The different values for four common methods of modifying incoming damage.
  * Two of the four resistances are directly paired with forms of incoming damage.
  * This is for defining pure accessor functions.
  */
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

/**
  * The different values for four common methods of modifying incoming damage.
  * Two of the four resistances are directly paired with forms of incoming damage.
  * This is for defining both accessor and mutator functions.
  */
trait ResistanceProfileMutators extends ResistanceProfile {
  private var resistanceDirectHit : Int = 0
  private var resistanceSplash : Int = 0
  private var resistanceAggravated : Int = 0
  private var radiationShielding : Float = 0f

  def ResistanceDirectHit : Int = resistanceDirectHit

  def ResistanceDirectHit_=(resist : Int) : Int = {
    resistanceDirectHit = resist
    ResistanceDirectHit
  }

  def ResistanceSplash : Int = resistanceSplash

  def ResistanceSplash_=(resist : Int) : Int = {
    resistanceSplash = resist
    ResistanceSplash
  }

  def ResistanceAggravated : Int = resistanceAggravated

  def ResistanceAggravated_=(resist : Int) : Int = {
    resistanceAggravated = resist
    ResistanceAggravated
  }

  def RadiationShielding : Float = radiationShielding

  def RadiationShielding_=(resist : Float) : Float = {
    radiationShielding = resist
    RadiationShielding
  }
}
