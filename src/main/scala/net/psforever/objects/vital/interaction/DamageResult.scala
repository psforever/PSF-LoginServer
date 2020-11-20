// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.interaction

import net.psforever.objects.ballistics.{AggravatedDamage, SourceEntry}
import net.psforever.objects.equipment.TargetValidation
import net.psforever.objects.vital.base.DamageType

/**
  * But one thing's sure. The player is hurt, attacked, and somebody's responsible.
  * @param attacker the source of the damage
  * @param defender the recipient of the damage
  * @param implement how the damage was invoked;
  *                  the object id of the method of punishment, used for reporting
  */
final case class Adversarial(attacker: SourceEntry, defender: SourceEntry, implement: Int)

/**
  * The outcome of the damage interaction, after all the numbers have been processed and properly applied.
  * References relevent special effects of the damage
  * without having to explore the specific reason for the interaction.
  */
trait DamageResult {
  def interaction: DamageInteraction

  def damageType: DamageType.Value

  def damageTypes: Set[DamageType.Value]

  def causesJammering: Boolean

  def jammering: List[(TargetValidation, Int)]

  def causesAggravation: Boolean

  def aggravation: Option[AggravatedDamage]

  def adversarial: Option[Adversarial]
}
