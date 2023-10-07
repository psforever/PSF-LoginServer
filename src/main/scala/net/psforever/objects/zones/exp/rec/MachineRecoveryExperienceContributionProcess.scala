// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.{DamagingActivity, InGameActivity, RepairFromEquipment, RepairingActivity}
import net.psforever.objects.zones.exp.{ContributionStats, Support, WeaponStats}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

class MachineRecoveryExperienceContributionProcess(
                                                    private val faction : PlanetSideEmpire.Value,
                                                    private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                    eventOutputType: String,
                                                    private val excludedTargets: mutable.ListBuffer[SourceUniqueness] = mutable.ListBuffer()
                                                  ) extends RecoveryExperienceContributionProcess(faction, contributions) {
  def submit(history: List[InGameActivity]): Unit = {
    history.foreach {
      case d: DamagingActivity if d.amount == d.health =>
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
      case r: RepairFromEquipment if !excludedTargets.contains(r.user.unique) =>
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

  override def output(): mutable.LongMap[ContributionStats] = {
    super.output().map { case (id, stats) =>
      val weps = stats.weapons
        .groupBy(_.equipment)
        .map { case (wrapper, entries) =>
          val size = entries.size
          val newTime = entries.maxBy(_.time).time
          Support.calculateSupportExperience(
            eventOutputType,
            WeaponStats(wrapper, size, entries.foldLeft(0)(_ + _.amount), newTime, 1f)
          )
        }
        .toSeq
      (id, stats.copy(weapons = weps))
    }
  }
}
