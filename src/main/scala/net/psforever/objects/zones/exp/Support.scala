// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.{InGameActivity, ReconstructionActivity, RepairFromExoSuitChange, SpawningActivity}
import net.psforever.types.{ExoSuitType, PlanetSideEmpire}

import scala.collection.mutable

/**
 * Functions to assist experience calculation and history manipulation and analysis.
 */
object Support {
  /**
   * Calculate a base experience value to consider additional reasons for points.
   * @param victim player to which a final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @return the value of the kill in what the game called "battle experience points"
   * @see `Support.wasEverAMax`
   */
  private[exp] def baseExperience(
                                   victim: PlayerSource,
                                   history: Iterable[InGameActivity]
                                 ): Long = {
    val lifespan = (history.headOption, history.lastOption) match {
      case (Some(spawn), Some(death)) => death.time - spawn.time
      case _                          => 0L
    }
    val base = if (Support.wasEverAMax(victim, history)) {
      250L
    } else if (victim.Seated || victim.progress.kills.nonEmpty) {
      100L
    } else if (lifespan > 15000L) {
      50L
    } else {
      1L
    }
    if (base > 1) {
      //black ops modifier
      //TODO x10
      base
    } else {
      base
    }
  }

  /**
   * Combine two quantitative records into one, maintaining only the original entries.
   * @param first one quantitative record
   * @param second another quantitative record
   * @param combiner mechanism for determining how to combine quantitative records;
   *                 defaults to an additive combiner with a small multiplier value
   * @return the combined quantitative records
   * @see `defaultAdditiveOutputCombiner`
   * @see `onlyOriginalAssistEntriesIterable`
   */
  private[exp] def onlyOriginalAssistEntries(
                                              first: mutable.LongMap[ContributionStatsOutput],
                                              second: mutable.LongMap[ContributionStatsOutput],
                                              combiner: (ContributionStatsOutput, ContributionStatsOutput)=>ContributionStatsOutput =
                                                defaultAdditiveOutputCombiner(multiplier = 0.05f)
                                            ): Iterable[ContributionStatsOutput] = {
    onlyOriginalAssistEntriesIterable(first.values, second.values, combiner)
  }

  /**
   * Combine two quantitative records into one, maintaining only the original entries.
   * @param first one quantitative record
   * @param second another quantitative record
   * @param combiner mechanism for determining how to combine quantitative records;
   *                 defaults to an additive combiner with a small multiplier value
   * @return the combined quantitative records
   * @see `defaultAdditiveOutputCombiner`
   */
  private[exp] def onlyOriginalAssistEntriesIterable(
                                                      first: Iterable[ContributionStatsOutput],
                                                      second: Iterable[ContributionStatsOutput],
                                                      combiner: (ContributionStatsOutput, ContributionStatsOutput)=>ContributionStatsOutput =
                                                        defaultAdditiveOutputCombiner(multiplier = 0.05f)
                                                    ): Iterable[ContributionStatsOutput] = {
    if (second.isEmpty) {
      first
    } else if (first.isEmpty) {
      second
    } else {
      //overlap discriminated by percentage
      val shared: mutable.LongMap[ContributionStatsOutput] = mutable.LongMap[ContributionStatsOutput]()
      for {
        h @ ContributionStatsOutput(hid, _, _) <- first
        a @ ContributionStatsOutput(aid, _, _) <- second
        out = combiner(h, a)
        id = out.player.CharId
        if hid == aid && shared.put(id, out).isEmpty
      } yield ()
      val sharedKeys = shared.keys
      (first ++ second).filterNot { case ContributionStatsOutput(id, _, _) => sharedKeys.exists(_ == id.CharId) } ++ shared.values
    }
  }

  /**
   * Combine two quantitative records into one, maintaining only the original entries.
   * @param multiplier adjust the combined
   * @param first one quantitative record
   * @param second another quantitative record
   * @return the combined quantitative records
   */
  private def defaultAdditiveOutputCombiner(
                                             multiplier: Float
                                           )
                                           (
                                             first: ContributionStatsOutput,
                                             second: ContributionStatsOutput
                                           ): ContributionStatsOutput = {
    if (first.percentage < second.percentage)
      second.copy(implements = (second.implements ++ first.implements).distinct, percentage = first.percentage + second.implements.size * multiplier)
    else
      first.copy(implements = (first.implements ++ second.implements).distinct, percentage = second.percentage + second.implements.size * multiplier)
  }

  /**
   * Take two sequences of equipment statistics
   * and combine both lists where overlap of the same equipment use is added together per field.
   * If one sequence comtains more elements of the same type of equipment use,
   * the additional entries may become lost.
   * @param first statistics in relation to equipment
   * @param second statistics in relation to equipment
   * @return statistics in relation to equipment
   */
  private[exp] def combineWeaponStats(
                                       first: Seq[WeaponStats],
                                       second: Seq[WeaponStats]
                                     ): Seq[WeaponStats] = {
    val (firstInSecond, firstAlone) = first.partition(firstStat => second.exists(_.equipment == firstStat.equipment))
    val (secondInFirst, secondAlone) = second.partition(secondStat => firstInSecond.exists(_.equipment == secondStat.equipment))
    val combined = firstInSecond.flatMap { firstStat =>
      secondInFirst
        .filter(_.equipment == firstStat.equipment)
        .map { secondStat =>
          firstStat.copy(
            shots = firstStat.shots + secondStat.shots,
            amount = firstStat.amount + secondStat.amount,
            contributions = firstStat.contributions + secondStat.contributions,
            time = math.max(firstStat.time, secondStat.time)
          )
        }
    }
    firstAlone ++ secondAlone ++ combined
  }

  /**
   * Run a function against history, targeting a certain faction.
   * @param tallyFunc the history analysis function
   * @param history chronology of activity the game considers noteworthy
   * @param faction empire to target
   * @return quantitative record of activity in relation to the other players and their equipment
   */
  private[exp] def allocateContributors(
                                         tallyFunc: (List[InGameActivity], PlanetSideEmpire.Value, mutable.LongMap[ContributionStats]) => Any
                                       )
                                       (
                                         history: List[InGameActivity],
                                         faction: PlanetSideEmpire.Value
                                       ): mutable.LongMap[ContributionStats] = {
    /*
    players who have contributed to this death, and how much they have contributed<br>
    key - character identifier,
    value - (player, damage, total damage, number of shots)
    */
    val participants: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
    tallyFunc(history, faction, participants)
    participants
  }

  /**
   * You better not fail this purity test.
   * @param player player being tested
   * @param history chronology of activity the game considers noteworthy;
   *                allegedly associated with this player
   * @return `true`, if the player has ever committed a great shame;
   *         `false`, otherwise ... and it better be
   */
  private[exp] def wasEverAMax(player: PlayerSource, history: Iterable[InGameActivity]): Boolean = {
    player.ExoSuit == ExoSuitType.MAX || history.exists {
      case SpawningActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case ReconstructionActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case RepairFromExoSuitChange(suit, _) => suit == ExoSuitType.MAX
      case _                                => false
    }
  }
}
