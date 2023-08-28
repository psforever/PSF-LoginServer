// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.{DamagingActivity, InGameActivity, RepairFromEquipment, RepairFromTerminal, RepairingActivity, SupportActivityCausedByAnother}
import net.psforever.objects.zones.exp.KillContributions
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

class ArmorRecoveryExperienceContributionProcess(
                                                  private val faction : PlanetSideEmpire.Value,
                                                  private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                  private val excludedTargets: mutable.ListBuffer[SourceUniqueness] = mutable.ListBuffer()
                                                ) extends RecoveryExperienceContributionProcess(faction, contributions) {
  def submit(history: List[InGameActivity]): Unit = {
    history.foreach {
      case d: DamagingActivity if d.amount - d.health > 0 =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithDamagingActivity(
          d,
          d.amount - d.health,
          damageParticipants,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case rt: RepairFromTerminal =>
        val time = rt.time
        val users = KillContributions.contributeWithTerminalActivity(
          Seq((rt, rt.term, rt.term.hacked)),
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
          rt.term.Definition.ObjectId,
          faction,
          rt.amount,
          time,
          participants,
          damageInOrder,
          recoveryInOrder
        )
        damageInOrder = damage
        recoveryInOrder = recovery
      case r: RepairFromEquipment =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(
          r.user,
          r.equipment_def.ObjectId,
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
      case r: RepairingActivity =>
        val (damage, recovery) = RecoveryExperienceContribution.contributeWithRecoveryActivity(
          wepid = 0,
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
