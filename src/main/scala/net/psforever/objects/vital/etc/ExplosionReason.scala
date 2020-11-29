// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.{PlanetSideGameObject, Vehicle}
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.vital.{NoResistanceSelection, Vitality}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output

final case class ExplosionReason(source: DamageWithPosition) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Explosion

  def same(test: DamageReason): Boolean = false

  def adversary: Option[SourceEntry] = None
}

//TODO only works for vhicles right now
final case class ExplodingEntityReason(entity: VehicleDefinition, drm: DamageAndResistance, instigation: Option[DamageInteraction]) extends DamageReason {
  assert(entity.Explodes.nonEmpty, "causal entity does not explode")

  def source: DamageWithPosition = entity.Explodes.get

  def resolution: DamageResolution.Value = DamageResolution.Explosion

  def same(test: DamageReason): Boolean = test match {
    case eer: ExplodingEntityReason => eer.entity.ObjectId == entity.ObjectId
    case _                          => false
  }

  def adversary: Option[SourceEntry] = instigation match {
    case Some(prior) => prior.cause.adversary
    case None         => None
  }

  override def attribution: Int = entity.ObjectId

  override def calculate(data : DamageInteraction) : Output = {
    ExplosionReason.drm(drm).calculate(data)
  }

  override def calculate(data : DamageInteraction, dtype : DamageType.Value) : Output = calculate(data)
}

object ExplosionReason {
  def drm(drm: DamageAndResistance): DamageAndResistance = new DamageResistanceModel {
    DamageUsing = drm.DamageUsing
    ResistUsing = NoResistanceSelection
    Model = drm.Model
  }
}

object ExplodingEntityReason {
  def apply(entity: Vehicle, target: PlanetSideGameObject with Vitality): ExplodingEntityReason =
    ExplodingEntityReason(entity.Definition, target.DamageModel, None)

  def apply(entity: Vehicle, target: PlanetSideGameObject with Vitality, cause: DamageInteraction): ExplodingEntityReason =
    ExplodingEntityReason(entity.Definition, target.DamageModel, Some(cause))
}
