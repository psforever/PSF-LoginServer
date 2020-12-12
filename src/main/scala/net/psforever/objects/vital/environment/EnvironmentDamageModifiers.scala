package net.psforever.objects.vital.environment

import net.psforever.objects.vital.base.{DamageModifiers, DamageReason}
import net.psforever.objects.vital.interaction.DamageInteraction

object EnvironmentDamageModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage : Int, data : DamageInteraction, cause : DamageReason) : Int = {
      cause match {
        case o : EnvironmentReason => calculate(damage, data, o)
        case _ => damage
      }
    }

    def calculate(damage : Int, data : DamageInteraction, cause : EnvironmentReason) : Int
  }
}
