// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.ballistics.{ProjectileResolution, ResolvedProjectile}
import net.psforever.objects.vital.damage.DamageSelection
import net.psforever.objects.vital.projectile.ProjectileCalculations
import net.psforever.objects.vital.resistance.ResistanceSelection
import net.psforever.objects.vital.resolution.ResolutionCalculations

/**
  * The functionality that is necessary for interaction of a vital game object with the rest of the game world.<br>
  * <br>
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * The actual implementation of how that works is left to the specific object and its interfaces, however.
  * The more involved values that are applied to the vital object are calculated by a series of functions
  * that contribute different values, e.g., the value for being damaged.
  * "Being damaged" is also not the same for all valid targets:
  * some targets don't utilize the same kinds of values in the same way as another,
  * and some targets utilize a different assortment of values than either of the first two examples.
  * The damage model is a common interface for producing those values
  * and reconciling those values with a valid target object
  * without much fuss.<br>
  * <br>
  * By default, nothing should do anything of substance.
  * @see `Vitality`
  */
trait DamageResistanceModel {
  /** the functionality that processes damage; required */
  private var damage : DamageSelection = NoDamageSelection

  /** the functionality that processes resistance; optional */
  private var resistance : ResistanceSelection = NoResistanceSelection

  /** the functionality that prepares for damage application actions; required */
  private var model : ResolutionCalculations.Form = NoResolutions.Calculate

  def Damage : DamageSelection = damage

  def Damage_=(selector : DamageSelection) : DamageSelection = {
    damage = selector
    Damage
  }

  def Resistance : ResistanceSelection = resistance

  def Resistance_=(selector : ResistanceSelection) : ResistanceSelection = {
    resistance = selector
    Resistance
  }

  def Model : ResolutionCalculations.Form = model

  def Model_=(selector : ResolutionCalculations.Form) : ResolutionCalculations.Form = {
    model = selector
    Model
  }

  /**
    * Magic stuff.
    * @param data the historical `ResolvedProjectile` information
    * @return a function literal that encapsulates delayed modification instructions for certain objects
    */
  def Calculate(data : ResolvedProjectile) : ResolutionCalculations.Output = {
    val dam : ProjectileCalculations.Form = Damage(data)
    val res : ProjectileCalculations.Form = Resistance(data)
    Model(dam, res, data)
  }

  /**
    * Magic stuff.
    * @param data the historical `ResolvedProjectile` information
    * @param resolution an explicit damage resolution overriding the one in the `ResolvedProjectile` object
    * @return a function literal that encapsulates delayed modification instructions for certain objects
    */
  def Calculate(data : ResolvedProjectile, resolution : ProjectileResolution.Value) : ResolutionCalculations.Output = {
    val dam : ProjectileCalculations.Form = Damage(resolution)
    val res : ProjectileCalculations.Form = Resistance(resolution)
    Model(dam, res, data)
  }
}
