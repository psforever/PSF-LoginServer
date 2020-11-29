// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.damage

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ballistics._
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.types.Vector3

/** The input value is the same as the output value. */
case object SameHit extends DamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int = damage
}

/**
  * The input value degrades (lessens)
  * the further the distance between the point of origin (target position)
  * and the point of encounter (`hitPos`) of its vector.
  * If the value is encountered beyond its maximum radial distance, the value is zero'd.
  */
case object RadialDegrade extends DamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int =
    DamageModifierFunctions.radialDegradeFunction(damage, data, cause)
}

/**
  * The input value degrades (lessens)
  * to the percentage of its original value
  * if the target is a vehicle with no shields.
  * Specifically used for the `galaxy_gunship`.
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

object DamageModifierFunctions {
  /**
    * The input value degrades (lessens)
    * the further the distance between the point of origin (target position)
    * and the point of encounter (`hitPos`) of its vector (projectile).
    * If the value is encountered beyond its maximum radial distance, the value is zero'd.
    */
  def radialDegradeFunction(damage: Int, data: DamageInteraction, cause: DamageReason): Int = {
    cause.source match {
      case withPosition: DamageWithPosition =>
        val distance  = Vector3.Distance(data.hitPos, data.target.Position)
        val radius    = withPosition.DamageRadius
        val radiusMin = withPosition.DamageRadiusMin
        if (distance <= radiusMin) {
          damage
        } else if (distance <= radius) {
          //damage - (damage * profile.DamageAtEdge * (distance - radiusMin) / (radius - radiusMin)).toInt
          val base = withPosition.DamageAtEdge
          val radi = radius - radiusMin
          (damage * ((1 - base) * ((radi - (distance - radiusMin)) / radi) + base)).toInt
        } else {
          0
        }
      case _ =>
        damage
    }
  }
}
