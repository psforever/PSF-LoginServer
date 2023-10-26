// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.collision

import net.psforever.objects.definition.ExoSuitDefinition
import net.psforever.objects.sourcing.{DeployableSource, PlayerSource, VehicleSource}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.Vector3

/**
  * Falling damage is a product of the falling distance.
  */
case object GroundImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int =
    CollisionDamageModifierFunctions.calculateGroundImpact(damage, data, cause)
}

/**
  * Falling damage is a product of the falling distance.
  */
case object GroundImpactWith extends CollisionWithDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionWithReason): Int =
    CollisionDamageModifierFunctions.calculateGroundImpact(damage, data, cause)
}

/**
  * The damage of a lateral collision is a product of how fast one is reported moving at the time of impact.
  * As per the format, moving velocity is translated into a throttle gear related to maximum forward speed.
  * Driving at high velocity into an inelastic structure is bad for one's integrity.
  */
case object HeadonImpact extends CollisionDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionReason): Int = {
    val vel = Vector3.Magnitude(cause.velocity.xy)
    if (vel > 0.05f) {
      val definition = data.target.Definition
      val xy = definition.collision.xy
      damage + xy.hp(xy.throttle((vel + 0.5f) / definition.maxForwardSpeed))
    } else {
      damage
    }
  }
}

/**
  * The damage of a lateral collision is a product of how fast one is reported moving at the time of impact.
  * Vehicles colliding with infantry is a special case as vehicles have a canned amount of damage just for that target.
  * Deployables might be rigged for instant destruction the moment vehicles collide with them;
  * in any case, check the deployable for damage handling.
  * For all other targets, e.g., vehicles against other vehicles,
  * damage is a function of the velocity turned into a percentage of full throttle matched against tiers of damage.
  */
case object HeadonImpactWithEntity extends CollisionWithDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionWithReason): Int = {
    val vel = Vector3.Magnitude(cause.velocity.xy)
    (data.target, cause.collidedWith) match {
      case (p: PlayerSource, v: VehicleSource) =>
        //player hit by vehicle; compromise between momentum-force-damage and velocity-damage
        //the math here isn't perfect; 3D vector-velocity is being used in 1D momentum equations
        val definition = v.Definition
        val suit = ExoSuitDefinition.Select(p.ExoSuit, p.Faction).collision
        val pmass = suit.massFactor
        val pForceFactor = suit.forceFactor
        val vmass = definition.mass
        val maxAvtrDam = definition.collision.avatarCollisionDamageMax
        val maxForwardSpeed = definition.maxForwardSpeed
        val collisionTime = 1.5f //a drawn-out inelastic collision
        val pvel = p.Velocity.getOrElse(Vector3.Zero)
        val vvel = v.Velocity.getOrElse(Vector3.Zero)
        val velCntrMass = (pvel * pmass + vvel * vmass) / (pmass + vmass) //velocity of the center of mass
        val pvelFin = Vector3.neg(pvel - velCntrMass) + velCntrMass
        val damp = math.min(pmass * Vector3.Magnitude(pvelFin - pvel) / (pForceFactor * collisionTime), maxAvtrDam.toFloat)
        val dama = maxAvtrDam * 0.35f * (vel + 0.5f) / maxForwardSpeed
        damage + (if (damp > dama) {
          if (damp - dama > dama) {
            damp - dama
          } else {
            dama
          }
        } else {
          if (dama - damp > damp) {
            dama - damp
          } else {
            damp
          }
        }).toInt

      case (a: DeployableSource, b) =>
        //deployable hit by vehicle; anything but an OHKO will cause visual desync, but still should calculate
        val xy = a.Definition.collision.xy
        damage + xy.hp(xy.throttle((vel + 0.5f) / b.Definition.maxForwardSpeed))
      case (_, b: VehicleSource) if vel > 0.05f =>
        //(usually) vehicle hit by another vehicle; exchange damages results
        val xy = b.Definition.collision.xy
        damage + xy.hp(xy.throttle((vel + 0.5f) / b.Definition.maxForwardSpeed))
      case (a, _) if vel > 0.05f =>
        //something hit by something
        val xy = a.Definition.collision.xy
        damage + xy.hp(xy.throttle((vel + 0.5f) / a.Definition.maxForwardSpeed))
      case _ =>
        //moving too slowly
        damage
    }
  }
}

/**
  * When the target collides with something,
  * if the target is not faction related with the cause,
  * the target takes multiplied damage.
  * The tactical resonance area protection is identified by never moving (has no velocity).
  */
case class TrapCollisionDamageMultiplier(multiplier: Float) extends CollisionWithDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: CollisionWithReason): Int = {
    val target = data.target
    if (target.Velocity.nonEmpty && target.Faction != cause.collidedWith.Faction) {
      (multiplier * damage).toInt
    } else {
      damage
    }
  }
}

object CollisionDamageModifierFunctions {
  private[collision] def calculateGroundImpact(damage: Int, data: DamageInteraction, cause: CausedByColliding): Int = {
    val fall = cause.fall
    if (fall.toInt != 0) {
      val z = data.target.Definition.collision.z
      (damage + z.hp(z.height(fall + 0.5f))) / 3
    } else {
      damage / 3
    }
  }
}
