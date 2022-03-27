// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.ballistics.{PlayerSource, SourceEntry, Projectile => ActualProjectile}
import net.psforever.objects.vital.base._
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.{ProjectileDamageModifierFunctions, ProjectileReason}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a radiation cloud.
  * @param projectile the projectile that caused the damage
  * @param damageModel the model to be utilized in these calculations;
  *                    typically, but not always, defined by the target
  * @param radiationShielding the amount of reduction to radiation damage that occurs due to external reasons;
  *                           best utilized for protection extended to vehicle passengers
  */
final case class RadiationReason(
                                   projectile: ActualProjectile,
                                   damageModel: DamageAndResistance,
                                   radiationShielding: Float
                                 ) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Radiation

  def source: DamageProperties = projectile.profile

  def same(test: DamageReason): Boolean = {
    test match {
      case o: RadiationReason => o.projectile.id == projectile.id //can only be another projectile with the same uid
      case _ => false
    }
  }

  def adversary: Option[SourceEntry] = Some(projectile.owner)

  override def unstructuredModifiers: List[DamageModifiers.Mod] = List(ShieldAgainstRadiation)

  override def attribution: Int = projectile.attribute_to
}

object RadiationDamageModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int = {
      cause match {
        case o: RadiationReason => calculate(damage, data, o)
        case _ => damage
      }
    }

    def calculate(damage: Int, data: DamageInteraction, cause: RadiationReason): Int
  }
}

/**
  * If the damage is caused by a projectile that emits a field that permeates vehicle armor,
  * determine by how much the traversed armor's shielding reduces the damage.
  */
case object ShieldAgainstRadiation extends RadiationDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: RadiationReason): Int = {
    if (data.resolution == DamageResolution.Radiation) {
      data.target match {
        case _: PlayerSource =>
          damage - (damage * cause.radiationShielding).toInt
        case _ =>
          0
      }
    } else {
      damage
    }
  }
}

/**
  * The initial application of aggravated damage against an infantry target
  * due to interaction with a radiation field
  * where the specific damage component is `Splash`.
  */
case object InfantryAggravatedRadiation extends RadiationDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: RadiationReason): Int = {
    ProjectileDamageModifierFunctions.baseAggravatedFormula(
      DamageResolution.Radiation,
      DamageType.Splash
    )(damage, data, ProjectileReason(cause.resolution, cause.projectile, cause.damageModel))
  }
}

/**
  * The ongoing application of aggravated damage ticks against an infantry target
  * due to interaction with a radiation field
  * where the specific damage component is `Splash`.
  * This is called "burning" regardless of what the active aura effect actually is.
  */
case object InfantryAggravatedRadiationBurn extends RadiationDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: RadiationReason): Int = {
    ProjectileDamageModifierFunctions.baseAggravatedBurnFormula(
      DamageResolution.Radiation,
      DamageType.Splash
    )(damage, data, ProjectileReason(cause.resolution, cause.projectile, cause.damageModel))
  }
}
