// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.{DamageType, StandardDamageProfile}

/**
  * The different values for four common methods of modifying incoming damage.
  * Two of the four resistances are directly paired with forms of incoming damage.
  * This is for defining pure accessor functions.
  */
trait ResistanceProfile {
  def Subtract: DamageProfile

  def ResistanceDirectHit: Int

  def ResistanceSplash: Int

  def ResistanceAggravated: Int

  def RadiationShielding: Float

  def Resist(dtype: DamageType.Value): Float = {
    dtype match {
      case DamageType.Direct     => ResistanceDirectHit.toFloat
      case DamageType.Splash     => ResistanceSplash.toFloat
      case DamageType.Aggravated => ResistanceAggravated.toFloat
      case DamageType.Radiation  => RadiationShielding
      case _                     => 0f
    }
  }
}

/**
  * The different values for four common methods of modifying incoming damage.
  * Two of the four resistances are directly paired with forms of incoming damage.
  * This is for defining both accessor and mutator functions.
  */
trait ResistanceProfileMutators extends ResistanceProfile {
  private val subtract: DamageProfile = new StandardDamageProfile {
    //subtract numbers are always negative modifiers
    override def Damage0_=(damage: Int): Int = super.Damage0_=(if (damage < 1) damage else -damage)

    override def Damage1_=(damage: Int): Int = super.Damage1_=(if (damage < 1) damage else -damage)

    override def Damage2_=(damage: Int): Int = super.Damage2_=(if (damage < 1) damage else -damage)

    override def Damage3_=(damage: Int): Int = super.Damage3_=(if (damage < 1) damage else -damage)

    override def Damage4_=(damage: Int): Int = super.Damage4_=(if (damage < 1) damage else -damage)
  }
  private var resistanceDirectHit: Int  = 0
  private var resistanceSplash: Int     = 0
  private var resistanceAggravated: Int = 0
  private var radiationShielding: Float = 0f

  def Subtract: DamageProfile = subtract

  def ResistanceDirectHit: Int = resistanceDirectHit

  def ResistanceDirectHit_=(resist: Int): Int = {
    resistanceDirectHit = resist
    ResistanceDirectHit
  }

  def ResistanceSplash: Int = resistanceSplash

  def ResistanceSplash_=(resist: Int): Int = {
    resistanceSplash = resist
    ResistanceSplash
  }

  def ResistanceAggravated: Int = resistanceAggravated

  def ResistanceAggravated_=(resist: Int): Int = {
    resistanceAggravated = resist
    ResistanceAggravated
  }

  def RadiationShielding: Float = radiationShielding

  def RadiationShielding_=(resist: Float): Float = {
    radiationShielding = resist
    RadiationShielding
  }
}
