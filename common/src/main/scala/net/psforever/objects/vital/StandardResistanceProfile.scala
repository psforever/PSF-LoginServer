// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.resistance.ResistanceProfile

/**
  * The different values for four common methods of modifying incoming damage.
  * Two of the four resistances are directly paired with forms of incoming damage.
  * This is for defining pure accessor functions,
  * based on the assumption that the implementing object's `Definition` is the primary `ResistanceProfile`.
  */
trait StandardResistanceProfile extends ResistanceProfile {
  this : PlanetSideGameObject =>
  //actually check that this will work for this implementing class
  assert(Definition.isInstanceOf[ResistanceProfile], s"$this object definition must extend ResistanceProfile")
  private val resistDef = Definition.asInstanceOf[ResistanceProfile] //cast only once

  def Subtract : DamageProfile = resistDef.Subtract

  def ResistanceDirectHit : Int = resistDef.ResistanceDirectHit

  def ResistanceSplash : Int = resistDef.ResistanceDirectHit

  def ResistanceAggravated : Int = resistDef.ResistanceDirectHit

  def RadiationShielding : Float = resistDef.ResistanceDirectHit
}
