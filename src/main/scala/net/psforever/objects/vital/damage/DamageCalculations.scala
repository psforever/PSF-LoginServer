// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.vital.base.DamageInteraction
import net.psforever.objects.vital.prop.DamageProfile

/**
  * A series of methods for extraction of the base damage against a given target type
  * as well as incorporating damage modifiers from the other aspects of the interaction.
  */
object DamageCalculations {
  type Selector = DamageProfile => Int

  //raw damage selectors
  def AgainstNothing(profile : DamageProfile) : Int = 0

  def AgainstExoSuit(profile : DamageProfile) : Int = profile.Damage0

  def AgainstVehicle(profile : DamageProfile) : Int = profile.Damage1

  def AgainstAircraft(profile : DamageProfile) : Int = profile.Damage2

  def AgainstMaxSuit(profile : DamageProfile) : Int = profile.Damage3

  def AgainstBFR(profile : DamageProfile) : Int = profile.Damage4

  /**
    * Get damage information from a series of profiles related to the weapon discharge.
    * @param selector the function that recovers the damage value
    * @param data     na
    * @return the accumulated damage value
    */
  def WithModifiers(selector: DamageProfile => Int, data: DamageInteraction) : Int = {
    val cause = data.cause
    val source = cause.source
    val target = data.target
    //base damage + static modifiers
    val staticModifiers = cause.staticModifiers ++
                          (if (source.UseDamage1Subtract) List(target.Modifiers.Subtract) else Nil)
    //unstructured modifiers (their ordering is intentional)
    val unstructuredModifiers = cause.unstructuredModifiers ++
                                source.Modifiers ++ target.Definition.Modifiers
    //apply
    var damage = selector(source) + staticModifiers.foldLeft(0)(_ + selector(_))
    unstructuredModifiers.foreach { mod => damage = mod.calculate(damage, data) }
    damage
  }
}
