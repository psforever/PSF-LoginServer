// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

final case class PainboxReason(entity: Painbox) extends DamageReason {
  private val definition = entity.Definition
  assert(definition.innateDamage.nonEmpty, s"causal entity '${definition.Name}' does not emit pain field")

  def source: DamageWithPosition = definition.innateDamage.get

  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = test match {
    case eer: PainboxReason         => eer.entity eq entity
    case _                          => false
  }

  def adversary: Option[SourceEntry] = None

  def damageModel : DamageAndResistance = PainboxReason.drm
}

object PainboxReason {
  /** damage0, no resisting, quick and simple */
  val drm = new DamageResistanceModel {
    DamageUsing = DamageCalculations.AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }
}
