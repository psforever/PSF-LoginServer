// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.projectile

import net.psforever.objects.vital.base._

object ProjectileDamageModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage : Int, data : DamageInteraction, cause : DamageReason) : Int = {
      cause match {
        case o : ProjectileReason => calculate(damage, data, o)
        case _ => damage
      }
    }

    def calculate(damage : Int, data : DamageInteraction, cause : ProjectileReason) : Int
  }
}
