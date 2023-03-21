// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.InGameActivity

object KillDeathAssists {
  private[exp] def calculateExperience(
                                          killer: PlayerSource,
                                          victim: PlayerSource,
                                          history: Iterable[InGameActivity]
                                        ): Long = {
    //base value (the kill experience before modifiers)
    val base = ExperienceCalculator.calculateExperience(victim, history)
    if (base > 1) {
      //battle rank disparity modifiers
      val battleRankDisparity = {
        import net.psforever.objects.avatar.BattleRank
        val killerLevel = BattleRank.withExperience(killer.bep).value
        val victimLevel = BattleRank.withExperience(victim.bep).value
        if (victimLevel > killerLevel || killerLevel - victimLevel < 6) {
          if (killerLevel < 7) {
            6 * victimLevel + 10
          } else if (killerLevel < 12) {
            (12 - killerLevel) * victimLevel + 10
          } else if (killerLevel < 25) {
            25 + victimLevel - killerLevel
          } else {
            25
          }
        } else {
          math.floor(-0.15f * base - killerLevel + victimLevel).toLong
        }
      }
      math.max(1, base + battleRankDisparity)
    } else {
      base
    }
  }
}
