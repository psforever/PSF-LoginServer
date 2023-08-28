// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.{DamagingActivity, HealFromEquipment, HealFromTerminal, HealingActivity, InGameActivity, RevivingActivity, SupportActivityCausedByAnother}
import net.psforever.objects.zones.exp.KillContributions
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

private class HealthRecoveryExperienceContributionProcess(
                                                           private val faction : PlanetSideEmpire.Value,
                                                           private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                           private val excludedTargets: mutable.ListBuffer[SourceUniqueness] = mutable.ListBuffer()
                                                         ) extends RecoveryExperienceContributionProcess(faction, contributions) {
  def submit(history: List[InGameActivity]): Unit = {
    history.foreach {
      case d: DamagingActivity if d.health > 0 =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithDamagingActivity(
          d,
          d.health,
          damageParticipants,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case ht: HealFromTerminal =>
        val time = ht.time
        val users = KillContributions.contributeWithTerminalActivity(
          Seq((ht, ht.term, ht.term.hacked)),
          faction,
          contributions,
          contributionsBy,
          excludedTargets
        )
          .collect { case entry: SupportActivityCausedByAnother => entry }
          .groupBy(_.user.unique)
          .map(_._2.head.user)
          .toSeq
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithSupportRecoveryActivity(
          users,
          ht.term.Definition.ObjectId,
          faction,
          ht.amount,
          time,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealFromEquipment =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(
          h.user,
          h.equipment_def.ObjectId,
          faction,
          h.amount,
          h.time,
          damageParticipants,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case h: HealingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(
          wepid = 0,
          faction,
          h.amount,
          h.time,
          damageParticipants,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RevivingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(
          r.equipment.ObjectId,
          faction,
          r.amount,
          r.time,
          damageParticipants,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case _ => ()
    }
  }
}
