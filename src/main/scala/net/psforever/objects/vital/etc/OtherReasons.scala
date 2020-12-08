// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance

final case class EnvironmentReason(body: Any, source: DamageProperties) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Unresolved

  def same(test: DamageReason): Boolean = {
    test match {
      case o : EnvironmentReason => body == o.body //TODO eq
      case _ => false
    }
  }

  def adversary: Option[SourceEntry] = None

  def damageModel: DamageAndResistance = null
}
