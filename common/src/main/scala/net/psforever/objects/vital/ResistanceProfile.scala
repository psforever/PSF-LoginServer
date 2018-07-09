// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject

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

trait StandardResistanceProfile extends ResistanceProfile {
  this : PlanetSideGameObject =>
  assert(Definition.isInstanceOf[ResistanceProfile], s"$this object definition must extend ResistanceProfile")
  private val resistDef = Definition.asInstanceOf[ResistanceProfile] //cast only once

  def ResistanceDirectHit : Int = resistDef.ResistanceDirectHit

  def ResistanceSplash : Int = resistDef.ResistanceDirectHit

  def ResistanceAggravated : Int = resistDef.ResistanceDirectHit

  def RadiationShielding : Float = resistDef.ResistanceDirectHit
}

trait ResistanceProfileMutators extends ResistanceProfile {
  this : Any =>

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
