// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.interaction

import net.psforever.objects.sourcing.SourceEntry

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
  */
final case class DamageResult(targetBefore: SourceEntry, targetAfter: SourceEntry, interaction: DamageInteraction) {
  def adversarial: Option[Adversarial] = {
    interaction.adversarial match {
      case Some(adversarial) => Some(Adversarial(adversarial.attacker, targetAfter, adversarial.implement))
      case None =>              None
    }
  }
}
