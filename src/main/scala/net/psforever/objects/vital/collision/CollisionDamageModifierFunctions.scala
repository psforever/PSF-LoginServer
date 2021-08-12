// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.Vector3

/**
  * Falling damage is a product of the falling distance applied to arbitrary damage values.
  */
case object GroundImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    val fall = cause.fall
    if (fall.toInt != 0) {
      val z = data.target.Definition.collision.z
      damage + z.hp(z.height(fall))
    } else {
      damage
    }
  }
}

case object HeadonImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    val definition = data.target.Definition
    val xy = definition.collision.xy
    damage + xy.hp(xy.throttle(Vector3.Magnitude(cause.velocity.xy) / definition.maxForwardSpeed))
  }
}

object CollisionDamageModifierFunctions {
  //intentionally blank
}
