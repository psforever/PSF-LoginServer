// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ballistics._
import net.psforever.objects.vital.base.{DamageInteraction, DamageModifiers, DamageReason}

/** The input value is the same as the output value. */
case object SameHit extends DamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int = damage
}

/**
  * The input value degrades (lessens)
  * to the percentage of its original value
  * if the target is a vehicle with no shields.
  * Mainly used for the `galaxy_gunship` vehicle.
  */
final case class GalaxyGunshipReduction(multiplier: Float) extends DamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int = {
    data.target match {
      case v: VehicleSource
        if v.Definition == GlobalDefinitions.galaxy_gunship && v.Shields == 0 =>
        (damage * multiplier).toInt
      case _ =>
        damage
    }
  }
}
