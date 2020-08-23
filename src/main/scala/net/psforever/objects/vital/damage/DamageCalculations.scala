// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.ballistics.ResolvedProjectile

/**
  * A series of methods for extraction of the base damage against a given target type
  * as well as incorporating damage modifiers from the other aspects of the interaction.
  */
object DamageCalculations {
  type Selector = DamageProfile => Int

  //raw damage selectors
  def AgainstNothing(profile: DamageProfile): Int = 0

  def AgainstExoSuit(profile: DamageProfile): Int = profile.Damage0

  def AgainstVehicle(profile: DamageProfile): Int = profile.Damage1

  def AgainstAircraft(profile: DamageProfile): Int = profile.Damage2

  def AgainstMaxSuit(profile: DamageProfile): Int = profile.Damage3

  def AgainstBFR(profile: DamageProfile): Int = profile.Damage4

  /**
    * Get damage information from a series of profiles related to the weapon discharge.
    * @param selector the function that recovers the damage value
    * @param data na
    * @return the accumulated damage value
    */
  def DamageWithModifiers(selector: DamageProfile => Int, data: ResolvedProjectile): Int = {
    val projectile = data.projectile
    val profile    = projectile.profile
    val fireMode   = projectile.fire_mode
    //static (additive and subtractive) modifiers
    val staticModifiers = if (profile.UseDamage1Subtract) {
      List(fireMode.Add, data.target.Modifiers.Subtract)
    } else {
      List(fireMode.Add)
    }
    //base damage + static modifiers
    var damage = selector(profile) + staticModifiers.foldLeft(0)(_ + selector(_))
    //unstructured modifiers (the order is intentional, however)
    (fireMode.Modifiers ++
      profile.Modifiers ++
      data.target.Definition.Modifiers)
      .foreach { mod => damage = mod.Calculate(damage, data) }
    damage
  }
}
