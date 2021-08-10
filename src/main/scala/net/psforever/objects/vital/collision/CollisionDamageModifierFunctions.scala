// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.ballistics.PlayerSource
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.Vector3

/**
  * Turn the velocity at the time of collision into damage.
  * Vehicles get full damage; squishy Infantry get halved damage.
  */
case object Impact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    data.target match {
      case _: PlayerSource => (Vector3.Magnitude(cause.velocity) * 0.5f).toInt
      case _               =>  Vector3.Magnitude(cause.velocity).toInt
    }
  }
}

object CollisionDamageModifierFunctions {
  //intentionally blank
}
