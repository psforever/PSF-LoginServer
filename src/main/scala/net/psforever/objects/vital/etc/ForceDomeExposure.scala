// Copyright (c) 2025 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.sourcing.{AmenitySource, SourceEntry}
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.prop.{DamageProperties, DamageWithPosition}
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

/**
 * A wrapper for a "damage source" in damage calculations that indicates a harmful interaction from a capitol force dome.
 * @param field the target of the field in question
 */
final case class ForceDomeExposure(field: SourceEntry)
  extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Collision

  def same(test: DamageReason): Boolean = test match {
    case eer: ForceDomeExposure => eer.field eq field
    case _                          => false
  }

  /**
   * Blame the capitol facility that is being protected.
   */
  override def attribution: Int = field match {
    case a: AmenitySource => a.installation.Definition.ObjectId
    case _ => field.Definition.ObjectId
  }

  override def source: DamageProperties = ForceDomeExposure.damageProperties

  override def damageModel: DamageAndResistance = ForceDomeExposure.drm

  /**
   * No one person will be blamed for this.
   */
  override def adversary: Option[SourceEntry] = None
}

object ForceDomeExposure {
  final val drm = new DamageResistanceModel {
    DamageUsing = DamageCalculations.AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }

  final val damageProperties = new DamageWithPosition {
    Damage0 = 99999
    DamageToHealthOnly = true
    DamageToVehicleOnly = true
    DamageToBattleframeOnly = true
  }
}

