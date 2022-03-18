// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason, DamageResolution}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.prop.{DamageProperties, DamageWithPosition}
import net.psforever.objects.vital.resolution.DamageAndResistance
import net.psforever.objects.zones.Zone

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a server-driven explosion occurring.
  * Some game objects cause area-of-effect damage upon being destroyed.
  * @see `VitalityDefinition.innateDamage`
  * @see `Zone.causesExplosion`
  * @param entity what is accredited as the source of the explosive yield
  * @param source information about the explosive yield
  * @param damageModel the model to be utilized in these calculations;
  *                    typically, but not always, defined by the target
  * @param instigation what previous event happened, if any, that caused this explosion
  */
final case class ExplodingEntityReason(
                                        entity: SourceEntry,
                                        source: DamageProperties,
                                        damageModel: DamageAndResistance,
                                        instigation: Option[DamageResult]
                                      ) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Explosion

  def same(test: DamageReason): Boolean = test match {
    case eer: ExplodingEntityReason => eer.entity eq entity
    case _                          => false
  }

  /** lay the blame on that which caused this explosion to occur */
  def adversary: Option[SourceEntry] = instigation match {
    case Some(prior) => prior.interaction.cause.adversary
    case None         => Some(entity)
  }

  override def attribution: Int = entity.Definition.ObjectId
}

object ExplodingEntityReason {
  /**
    * An overloaded constructor for a wrapper for a "damage source" in damage calculations.
    * @param entity the source of the explosive yield
    * @param damageModel the model to be utilized in these calculations
    * @param instigation what previous event happened, if any, that caused this explosion
    * @return an `ExplodingEntityReason` entity
    */
  def apply(
             entity: PlanetSideGameObject with FactionAffinity with Vitality,
             damageModel: DamageAndResistance,
             instigation: Option[DamageResult]
           ): ExplodingEntityReason = {
    val definition = entity.Definition.asInstanceOf[ObjectDefinition with VitalityDefinition]
    assert(definition.innateDamage.nonEmpty, "causal entity does not explode")
    ExplodingEntityReason(SourceEntry(entity), definition.innateDamage.get, damageModel, instigation)
  }
}

object ExplodingDamageModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage : Int, data : DamageInteraction, cause : DamageReason) : Int = {
      cause match {
        case o: ExplodingEntityReason => calculate(damage, data, o)
        case _ => damage
      }
    }

    def calculate(damage : Int, data : DamageInteraction, cause : ExplodingEntityReason) : Int
  }
}

/**
  * A variation of the normal radial damage degradation
  * that uses the geometric representations of the exploding entity and of the affected target
  * in its calculations that determine the distance between them.
  * @see `DamageModifierFunctions.RadialDegrade`
  */
case object ExplodingRadialDegrade extends ExplodingDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ExplodingEntityReason): Int = {
    cause.source match {
      case withPosition: DamageWithPosition =>
        val radius    = withPosition.DamageRadius
        val radiusMin = withPosition.DamageRadiusMin
        val distance = math.sqrt(Zone.distanceCheck(
          cause.entity.Definition.asInstanceOf[ObjectDefinition].Geometry(cause.entity),
          data.target.Definition.Geometry(data.target)
        ))
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

case object ExplosionDamagesOnlyAbove extends ExplodingDamageModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ExplodingEntityReason): Int = {
    if (data.target.Position.z <= data.hitPos.z) {
      damage
    } else {
      0
    }
  }
}
