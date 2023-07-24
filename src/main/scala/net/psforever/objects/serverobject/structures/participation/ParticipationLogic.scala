// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.types.PlanetSideEmpire

//noinspection ScalaUnusedSymbol
trait ParticipationLogic {
  def building: Building
  def TryUpdate(): Unit
  /**
   * na
   * @param defenderFaction those attempting to stop the hack
   *                        the `terminal` (above) and facility originally belonged to this empire
   * @param attackingFaction those attempting to progress the hack;
   *                         the `hacker` (below) belongs to this empire
   * @param hacker the player who hacked the capture terminal (above)
   * @param hackTime how long the over-all facility hack allows or requires
   * @param completionTime how long the facility hacking process lasted
   * @param isResecured whether `defendingFaction` or the `attackingFaction` succeeded;
   *                    the latter is called a "capture",
   *                    while the former is a "resecure"
   */
  def RewardFacilityCapture(
                             defenderFaction: PlanetSideEmpire.Value,
                             attackingFaction: PlanetSideEmpire.Value,
                             hacker: PlayerSource,
                             hackTime: Long,
                             completionTime: Long,
                             isResecured: Boolean
                           ): Unit

  def PlayerContribution(timeDelay: Long = 600): Map[Long, Float]
}
