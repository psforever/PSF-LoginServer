// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.environment

import net.psforever.objects.ballistics.PlayerSource
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.serverobject.environment.EnvironmentAttribute

/**
  * The deeper you move into lava, the greater the amount of health you burn through.
  * Vehicles take significant damage.
  * What do you hope to achieve by wading through molten rock anyway?
  */
case object LavaDepth extends EnvironmentDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: EnvironmentReason): Int = {
    if (cause.body.attribute == EnvironmentAttribute.Lava) {
      val depth: Float = scala.math.max(0, (cause.body.collision.altitude - data.target.Position.z).toFloat)
      data.target match {
        case _: PlayerSource =>
          (damage * (1f + depth)).toInt
        case t =>
          damage + (0.05f * depth * t.Definition.MaxHealth).toInt
      }
    } else {
      damage
    }
  }
}

object EnvironmentDamageModifierFunctions {
  //intentionally blank
}
