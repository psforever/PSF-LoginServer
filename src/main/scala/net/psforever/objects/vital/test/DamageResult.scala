// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.test

import net.psforever.objects.ballistics.{AggravatedDamage, SourceEntry}
import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.vital.DamageType

final case class Adversarial(attacker: SourceEntry, defender: SourceEntry, implement: Int)

trait DamageResult {
  def interaction: DamageInteraction

  def damageTypes: Set[DamageType.Value]

  def causesJammering: Boolean

  def jammering: List[(TargetValidation, Int)]

  def causesAggravation: Boolean

  def aggravation: Option[AggravatedDamage]

  def adversarial: Option[Adversarial]
}
