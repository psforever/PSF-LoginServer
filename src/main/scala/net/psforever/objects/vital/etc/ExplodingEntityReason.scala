// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.resolution.DamageAndResistance

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a server-driven explosion occurring.
  * Some game objects cause area-of-effect damage upon being destroyed.
  * @see `VitalityDefinition.explodes`
  * @see `VitalityDefinition.innateDamage`
  * @see `Zone.causesExplosion`
  * @param entity the source of the explosive yield
  * @param damageModel the model to be utilized in these calculations;
  *                    typically, but not always, defined by the target
  * @param instigation what previous event happened, if any, that caused this explosion
  */
final case class ExplodingEntityReason(
                                        entity: PlanetSideGameObject with Vitality,
                                        damageModel: DamageAndResistance,
                                        instigation: Option[DamageResult]
                                      ) extends DamageReason {
  val definition = entity.Definition.asInstanceOf[ObjectDefinition with VitalityDefinition]
  assert(definition.explodes && definition.innateDamage.nonEmpty, "causal entity does not explode")

  def source: DamageWithPosition = definition.innateDamage.get

  def resolution: DamageResolution.Value = DamageResolution.Explosion

  def same(test: DamageReason): Boolean = test match {
    case eer: ExplodingEntityReason => eer.entity eq entity
    case _                          => false
  }

  /** lay the blame on that which caused this explosion to occur */
  def adversary: Option[SourceEntry] = instigation match {
    case Some(prior) => prior.interaction.cause.adversary
    case None         => None
  }

  /** the entity that exploded is the source of the damage */
  override def attribution: Int = definition.ObjectId
}
