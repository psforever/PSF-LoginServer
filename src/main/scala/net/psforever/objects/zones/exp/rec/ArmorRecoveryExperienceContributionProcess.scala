// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.{DamagingActivity, InGameActivity, RepairFromEquipment, RepairingActivity}
import net.psforever.types.PlanetSideEmpire

class ArmorRecoveryExperienceContributionProcess(
                                                  private val faction : PlanetSideEmpire.Value,
                                                  private val contributions: Map[SourceUniqueness, List[InGameActivity]]
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
