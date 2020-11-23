// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.vital.{AnyResolutions, NoResistanceSelection}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output

/**
  * Suicidal thoughts ...
  */
final case class SuicideReason()
  extends DamageReason {
  def source: DamageProperties = SuicideReason.damageProperties

  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = {
    test.source eq source
  }

  override def calculate(data : DamageInteraction) : Output = {
    SuicideReason.drm.calculate(data)
  }

  override def calculate(data : DamageInteraction, dtype : DamageType.Value) : Output = calculate(data)
}

object SuicideReason {
  val damageProperties = new DamageProperties {
    Damage0 = 99999
    DamageToHealthOnly = true
    DamageToVehicleOnly = true
    DamageToBattleframeOnly = true
  }

  val drm = new DamageResistanceModel {
    DamageUsing = DamageCalculations.AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = AnyResolutions.calculate
  }
}
