// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.types.PlanetSideEmpire

case object NoParticipation extends ParticipationLogic {
  def building: Building = Building.NoBuilding
  def TryUpdate(): Unit = { /* nothing here */ }
  def RewardFacilityCapture(
                             defenderFaction: PlanetSideEmpire.Value,
                             attackingFaction: PlanetSideEmpire.Value,
                             hacker: PlayerSource,
                             hackTime: Long,
                             completionTime: Long,
                             isResecured: Boolean
                           ): Unit = { /* nothing here */ }
  override def PlayerContributionRaw: Map[Long, (Player, Int, Long)] = Map.empty[Long, (Player, Int, Long)]

  override def PlayerContribution(timeDelay: Long): Map[Long, Float] = Map.empty[Long, Float]
}
