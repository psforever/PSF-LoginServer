// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.Vector3

/**
  * Falling damage is a product of the falling distance.
  */
case object GroundImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    val fall = cause.fall
    if (fall.toInt != 0) {
      val z = data.target.Definition.collision.z
      damage + z.hp(z.height(fall + 0.5f))
    } else {
      damage
    }
  }
}

/**
  * The damage of a lateral collision is a prosduct of how fast one is reported moving at the time of impact.
  * As per the format, moving velocity is translated into a throttle gear related to maximum forward speed.
  * Driving at high velocity into an inelastic structure is bad for one's integrity.
  */
case object HeadonImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    val vel = Vector3.Magnitude(cause.velocity.xy)
    if (math.abs(vel) < 0.01f) {
      val definition = data.target.Definition
      val xy = definition.collision.xy
      damage + xy.hp(xy.throttle((vel + 0.5f) / definition.maxForwardSpeed))
    } else {
      damage
    }
  }
}

object CollisionDamageModifierFunctions {
  //intentionally blank
}
