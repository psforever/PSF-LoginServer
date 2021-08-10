// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.vital.base._
import net.psforever.objects.vital.interaction.DamageInteraction

object CollisionDamageModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage : Int, data : DamageInteraction, cause : DamageReason) : Int = {
      cause match {
        case o : CollisionReason => calculate(damage, data, o)
        case _ => damage
      }
    }

    def calculate(damage : Int, data : DamageInteraction, cause : CollisionReason) : Int
  }
}
