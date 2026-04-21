// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.{ExoSuitChange, InGameActivity, RevivingActivity, TerminalUsedActivity, DismountingActivity, MountingActivity, MountChange, VitalityDefinition}
import net.psforever.types.{ExoSuitType, PlanetSideEmpire}
import net.psforever.util.{Config, DefinitionUtil, ThreatAssessment, ThreatLevel}

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Functions to assist experience calculation and history manipulation and analysis.
 */
object Support {
  /** Almost nothing! */
  final val TheShortestLifeIsWorth: Long = 1L

  /**
   * Calculate the experience value to reflect the value of a player's lifespan.
   * @param victim player to which a final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @return the value of the kill in what the game called "battle experience points"
   */
  private[exp] def baseExperience(
                                   victim: PlayerSource,
                                   history: Iterable[InGameActivity]
                                 ): Long = {
    //setup
    val historyList = history.toList
    val withKills = victim.progress.kills.nonEmpty
    //TODO Issue #1259 - Use another method to capture time of death than current time ("kill shots" aren't working)
    /*
    val fullLifespan = (historyList.headOption, historyList.lastOption) match {
      case (Some(spawn), Some(death)) => death.time - spawn.time
     */
    val fullLifespan = historyList.headOption match {
      case Some(spawn) => System.currentTimeMillis() - spawn.time
      case _                          => 0L
    }
    val recordOfWornTimes = countTimeWhileExoSuitOrMounted(historyList)
      .map { case (id, time) => (id, (time * 0.001f).toLong) } // turn milliseconds into seconds
    //short life factors
    val shortLifeBonus = baseExperienceShortLifeFactors(
      victim,
      historyList,
      recordOfWornTimes,
      withKills,
      fullLifespan
    )
    if (shortLifeBonus > TheShortestLifeIsWorth) {
      //long life factors
      val longLifeBonus: Long = {
        val threat = baseExperienceLongLifeFactors(victim, recordOfWornTimes, defaultValue = 100f * shortLifeBonus.toFloat)
        if (withKills) {
          threat
        } else {
          (threat * 0.85f).toLong
        }
      }
      shortLifeBonus + longLifeBonus
    } else {
      //the shortest life is afforded no additional bonuses
      shortLifeBonus
    }
  }

  /**
   * Assuming a chronological history of player actions and interactions,
   * allocate every exo-suit use and mountable use to a time interval
   * and accumulates the sum of those time intervals.
   * The end result is a map association between exo-suits and vehicles and time that equipment has been used.
   * @param history chronology of activity the game considers noteworthy
   * @param initialExosuit start with this exo-suit type
   * @return mapping between equipment (object class ids) and the time that equipment has been used (ms);
   *         the "equipment" includes exo-suits and all noted mountable entities
   */
  private def countTimeWhileExoSuitOrMounted(
                                               history: List[InGameActivity],
                                               initialExosuit: ExoSuitType.Value = ExoSuitType.Standard
                                             ): Map[Int, Long] = {
    val wornTime: mutable.HashMap[Int, Long] = mutable.HashMap[Int, Long]()
    var currentSuit: Int = initialExosuit.id
    var lastActTime: Long = history.head.time
    var lastMountAct: Option[MountChange] = None
    //collect history events that encompass changes to exo-suits and to mounting conditions
    history.collect {
      case suitChange: ExoSuitChange =>
        updateEquippedEntry(
          currentSuit,
          suitChange.time - lastActTime,
          wornTime
        )
        currentSuit = suitChange.exosuit.id
        lastActTime = suitChange.time
      case mount: MountingActivity =>
        updateEquippedEntry(
          currentSuit,
          mount.time - lastActTime,
          wornTime
        )
        lastActTime = mount.time
        lastMountAct = Some(mount)
      case dismount: DismountingActivity
        if dismount.pairedEvent.isEmpty =>
        updateEquippedEntry(
          dismount.mount.Definition.ObjectId,
          dismount.time - lastActTime,
          wornTime
        )
        lastActTime = dismount.time
        lastMountAct = None
      case dismount: DismountingActivity =>
        updateEquippedEntry(
          dismount.mount.Definition.ObjectId,
          dismount.time - dismount.pairedEvent.get.time,
          wornTime
        )
        lastActTime = dismount.time
        lastMountAct = None
    }
    //no more changes; add remaining time from unresolved activity
    val lastTime = history.last.time
    lastMountAct
      .collect { mount =>
        //dying in a vehicle is a reason to care about the last mount activity
        updateEquippedEntry(
          mount.mount.Definition.ObjectId,
          lastTime - mount.time,
          wornTime
        )
        Some(mount)
      }
      .orElse {
        //dying while on foot
        updateEquippedEntry(
          currentSuit,
          lastTime - lastActTime,
          wornTime
        )
        None
      }
    wornTime.toMap
  }

  /**
   * ...
   * @param equipmentId the equipment
   * @param timePassed how long it was in use
   * @param wornTime mapping between equipment (object class ids) and the time that equipment has been used (ms)
   * @return the length of time the equipment was used
   */
  private def updateEquippedEntry(
                                   equipmentId: Int,
                                   timePassed: Long,
                                   wornTime: mutable.HashMap[Int, Long]
                                 ): Long = {
    wornTime
      .get(equipmentId)
      .collect {
        oldTime =>
          val time = oldTime + timePassed
          wornTime.update(equipmentId, time)
          time
      }
      .orElse {
        wornTime.update(equipmentId, timePassed)
        Some(timePassed)
      }
      .get
  }

  /**
   * Calculate the experience value to reflect the value of a player's short term lifespan.
   * In effect, determine a token experience value for short unproductive lives.
   * Four main conditions are outlined.
   * In order of elimination traversal:
   * was the player ever using a mechanized assault exo-suit,
   * did the player kill anything,
   * was the player mounted in a vehicle of turret for long enough for it to be considered,
   * and has the player been alive long enough?
   * @param player player to which a final interaction has reduced health to zero
   * @param history chronology of activity the game considers noteworthy
   * @param recordOfWornTimes between equipment (object class ids) and the time that equipment has been used (ms)
   * @param withKills consider that the victim killed an opponent in this past life
   * @param fullLifespan for how long this last life spanned
   * @return the value of the kill in what the game called "battle experience points"
   * @see `Config.app.game.experience.bep.base`
   */
  private def baseExperienceShortLifeFactors(
                                              player: PlayerSource,
                                              history: List[InGameActivity],
                                              recordOfWornTimes: Map[Int, Long],
                                              withKills: Boolean,
                                              fullLifespan: Long
                                            ): Long = {
    val bep = Config.app.game.experience.bep.base
    //TODO bops
    if (recordOfWornTimes.getOrElse(ExoSuitType.MAX.id, 0L) > 0L) { //see: Support.wasEverAMax
      bep.asMax
    } else if (withKills) {
      bep.withKills
    } else if (player.Seated || {
      val mountTime = recordOfWornTimes.collect { case (id, value) if id > 10 => value }.sum
      mountTime * 3L >= fullLifespan
    }) {
      bep.asMounted
    } else {
      val validMaturityTime = if (
        !history.head.isInstanceOf[RevivingActivity] ||
          history.exists(_.isInstanceOf[TerminalUsedActivity])
      ) {
        bep.maturityTime
      } else {
        0L
      }
      if (fullLifespan > validMaturityTime) {
        bep.mature
      } else {
        TheShortestLifeIsWorth
      }
    }
  }

  /**
   * Calculate the experience value to reflect the value of a player's full lifespan.
   * A lifespan is associated with conditions and states that can each be assigned a weight or value.
   * Summing up all of these conditions and states produces a reward value.
   * @param player player to which a final interaction has reduced health to zero
   * @param recordOfWornTimes between equipment (object class ids) and the time that equipment has been used (ms)
   * @return the value of the kill in what the game called "battle experience points"
   * @see `Config.app.game.experience.bep.lifeSpanThreatRate`
   * @see `Config.app.game.experience.bep.threatAssessmentOf`
   */
  private def baseExperienceLongLifeFactors(
                                             player: PlayerSource,
                                             recordOfWornTimes: Map[Int, Long],
                                             defaultValue: Float
                                           ): Long = {
    //awarded values for a target's lifespan based on the distribution of their tactical choices
    val individualThreatEstimates: Map[Int, Float] = calculateThreatEstimatesPerEntry(recordOfWornTimes)
    val totalThreatEstimate: Float = individualThreatEstimates.values.sum
    val maxThreatCapacity: Float = {
      val (exosuitTimes, otherTimes) = recordOfWornTimes.partition(_._1 < 10)
      calculateMaxThreatCapacityPerEntry(
        (if (exosuitTimes.values.sum > otherTimes.values.sum) {
          individualThreatEstimates.filter(_._1 < 10)
        } else {
          individualThreatEstimates.filter(_._1 > 10)
        }).maxByOption(_._2).map(_._1).getOrElse(0),
        defaultValue
      )
    }
    //menace modifier -> min = kills, max = 8 x kills
    val menace = (player.progress.kills.size.toFloat * (1f + Support.calculateMenace(player).toFloat)).toLong
    //last kill experience
    val lastKillExperience = player.progress.kills
      .lastOption
      .collect { kill =>
        val reduce = ((System.currentTimeMillis() - kill.time.toDate.getTime).toFloat * 0.001f).toLong
        math.max(0L, kill.experienceEarned - reduce)
      }
      .getOrElse(0L)
    //cap lifespan then add extra
    math.min(totalThreatEstimate, maxThreatCapacity).toLong + menace + lastKillExperience
  }

  /**
   * Calculate the reward available based on a tactical option by id.
   * @param recordOfWornTimes between equipment (object class ids) and the time that equipment has been used (ms)
   * @return value of the equipment
   */
  private def calculateThreatEstimatesPerEntry(recordOfWornTimes: Map[Int, Long]): Map[Int, Float] = {
    recordOfWornTimes.map {
      case (key, amount) => (key, amount * calculateThreatEstimatesPerEntry(key))
    }
  }

  /**
   * Calculate the reward available based on a tactical option by id.
   * If not listed in a previous table of values,
   * obtain the definition associated with the equipment id and test use the mass of the entity.
   * The default value is 0.
   * @param key equipment id used to collect the ceiling value
   * @return value of the equipment
   * @see `Config.app.game.experience.bep.threatAssessmentOf`
   * @see `VitalityDefinition.mass`
   */
  private def calculateThreatEstimatesPerEntry(key: Int): Float = {
    Config.app.game.experience.bep.lifeSpan.threatAssessmentOf
      .find { case ThreatAssessment(a, _) => a == key }
      .map(_.value)
      .getOrElse {
        getDefinitionById(key)
          .map(o => 2f + math.log10(o.mass.toDouble).toFloat)
          .getOrElse(0f)
      }
  }


  /**
   * Calculate the maximum possible reward available based on tactical options.
   * If not listed in a previous table of values,
   * obtain the definition associated with the equipment id and test use the maximum health of the entity.
   * @param key equipment id used to estimate one sample for the ceiling value
   * @param defaultValue what to use for an unresolved ceiling value;
   *                     defaults to 0
   * @return maximum value for this equipment
   * @see `Config.app.game.experience.bep.maxThreatLevel`
   * @see `VitalityDefinition.MaxHealth`
   */
  private def calculateMaxThreatCapacityPerEntry(
                                                  key: Int,
                                                  defaultValue: Float
                                                ): Float = {
    Config.app.game.experience.bep.lifeSpan.maxThreatLevel
      .find { case ThreatLevel(a, _) => a == key }
      .map(_.level.toFloat)
      .getOrElse {
        getDefinitionById(key)
          .map(_.MaxHealth.toFloat * 1.2f)
          .getOrElse(defaultValue)
      }
  }

  /**
   * ...
   * @param key equipment id
   * @return the definition if the definition can be found;
   *         `None`, otherwise
   * @see `DefinitionUtil.idToDefinition`
   * @see `GlobalDefinitions`
   * @see `VitalityDefinition`
   */
  private def getDefinitionById(key: Int): Option[VitalityDefinition] = {
    try {
      DefinitionUtil.idToDefinition(key) match {
        case o: VitalityDefinition => Some(o)
        case _                     => None
      }
    } catch {
      case _: Exception => None
    }
  }

  /**
   * "Menace" is a crude measurement of how much consistent destructive power a player has been demonstrating.
   * Within the last few kills, the rate of the player's killing speed is measured.
   * The measurement - a "streak" in modern lingo - is transformed into the form of an `Integer` for simplicity.
   * @param player the player
   * @param minimumKills number of kills needed before menace is considered
   * @param testValues time values to determine allowable delay between kills to qualify for a score rating;
   *                   three score ratings, so three values;
   *                   defaults to 20s, 10s, 5s (in ms)
   * @param maxDelayDiff time until the previous kill disqualifies menace;
   *                     exclusive amount of time allowed between qualifying entries;
   *                     default is 45s (in ms)
   * @param minDelayDiff inclusive amount of time difference allowed between valid entries;
   *                     default is 20s (in ms)
   * @param mercy a time value that can be used to continue a missed streak;
   *              defaults to 5s (in ms)
   * @return an integer between 0 and 7;
   *         0 is no kills,
   *         1 is some kills,
   *         2-7 is a menace score;
   *         there is no particular meaning behind different menace scores ascribed by this function
   *         but the range allows for progressive distinction
   * @see `qualifiedTimeDifferences`
   * @see `takeWhileLess`
   */
  private[exp] def calculateMenace(
                                    player: PlayerSource,
                                    minimumKills: Int = 3,
                                    testValues: Seq[Long] = Seq(20000L, 10000L, 5000L),
                                    maxDelayDiff: Long = 45000L,
                                    minDelayDiff: Long = 20000L,
                                    mercy: Long = 5000L
                                  ): Int = {
    //init
    val (minDiff, maxDiff) = (math.min(maxDelayDiff, maxDelayDiff), math.max(maxDelayDiff, maxDelayDiff))
    val valuesForTesting = testValues.padTo(3, ((maxDiff + minDiff) * 0.5f).toLong)
    //func
    val allKills = player.progress.kills
    //the very first kill must have been within the max delay (but does not count towards menace)
    if (allKills.headOption.exists { System.currentTimeMillis() - _.time.toDate.getTime < maxDiff}) {
      allKills match {
        case _ :: kills if kills.size > minimumKills =>
          val (continuations, restsBetweenKills) =
            qualifiedTimeDifferences(
              kills.map(_.time.toDate.getTime).iterator,
              maxValidDiffCount = 10,
              maxDiff,
              minDiff
            )
              .partition(_ > minDiff)
          math.max(
            1,
            math.floor(math.sqrt(
              math.max(0, takeWhileLess(restsBetweenKills, valuesForTesting.head, mercy).size - 1) + /*max=8*/
                math.max(0, takeWhileLess(restsBetweenKills, valuesForTesting(1), mercy).size - 5) * 3 + /*max=12*/
                math.max(0, takeWhileLess(restsBetweenKills, valuesForTesting(2), mercy = 1000L).size - 4) * 7 /*max=35*/
            ) - continuations.size)
          ).toInt
        case _ =>
          1
      }
    } else {
      0
    }
  }

  /**
   * Take a list of times
   * and produce a list of delays between those entries less than a maximum time delay.
   * These are considered "qualifying".
   * Count a certain number of time delays that fall within a minimum threshold
   * and stop when that minimum count is achieved.
   * These are considered "valid".
   * The final product should be a new list of the successive delays from the first list
   * containing both qualified and valid entries,
   * stopping at either the first unqualified delay or the last valid delay or at exhaustion of the original list.
   * @param iter unfiltered list of times (ms)
   * @param maxValidDiffCount maximum number of valid entries in the final list of time differences;
   *                          see `validTimeEntryCount`
   * @param maxDiff exclusive amount of time allowed between qualifying entries;
   *                include any time difference within this delay;
   *                these entries are "qualifying" but are not "valid"
   * @param minDiff inclusive amount of time difference allowed between valid entries;
   *                include time differences in this delay
   *                these entries are "valid" and should increment the counter `validTimeEntryCount`
   * @return list of qualifying time differences (ms)
   */
  /*
  Parameters governed by recursion:
  @param diffList ongoing list of qualifying time differences (ms)
  @param diffExtensionList accumulation of entries greater than the `minTimeEntryDiff`
                           but less that the `minTimeEntryDiff`;
                           holds qualifying time differences
                           that will be included before the next valid time difference
  @param validDiffCount currently number of valid time entries in the qualified time list;
                        see `maxValidTimeEntryCount`
  @param previousTime previous qualifying entry time;
                      by default, current time (ms)
  */
  @tailrec
  private def qualifiedTimeDifferences(
                                        iter: Iterator[Long],
                                        maxValidDiffCount: Int,
                                        maxDiff: Long,
                                        minDiff: Long,
                                        diffList: Seq[Long] = Nil,
                                        diffExtensionList: Seq[Long] = Nil,
                                        validDiffCount: Int = 0,
                                        previousTime: Long = System.currentTimeMillis()
                                      ): Iterable[Long] = {
    if (iter.hasNext && validDiffCount < maxValidDiffCount) {
      val nextTime = iter.next()
      val delay = previousTime - nextTime
      if (delay < maxDiff) {
        if (delay <= minDiff) {
          qualifiedTimeDifferences(
            iter,
            maxValidDiffCount,
            maxDiff,
            minDiff,
            diffList ++ (diffExtensionList :+ delay),
            Nil,
            validDiffCount + 1,
            nextTime
          )
        } else {
          qualifiedTimeDifferences(
            iter,
            maxValidDiffCount,
            maxDiff,
            minDiff,
            diffList,
            diffExtensionList :+ delay,
            validDiffCount,
            nextTime
          )
        }
      } else {
        diffList
      }
    } else {
      diffList
    }
  }

  /**
   * From a list of values, isolate all values less than than a test value.
   * @param list list of values
   * @param testValue test value that all valid values must be less than
   * @param mercy initial mercy value that values may be tested against being less than the test value
   * @return list of values less than the test value, including mercy
   */
  private def takeWhileLess(
                             list: Iterable[Long],
                             testValue: Long,
                             mercy: Long
                           ): Iterable[Long] = {
    var onGoingMercy: Long = mercy
    list.filter { value =>
      if (value < testValue) {
        true
      } else if (value - onGoingMercy < testValue) {
        //mercy is reduced every time it is utilized to find a valid value
        onGoingMercy = math.ceil(onGoingMercy * 0.8f).toLong
        true
      } else {
        false
      }
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
      case suitChange: ExoSuitChange => suitChange.exosuit == ExoSuitType.MAX
      case _                         => false
    }
  }

  /**
   * Take a weapon statistics entry and calculate the support experience value resulting from this support event.
   * The complete formula is:<br><br>
   * `base + shots-multplier * ln(shots^exp + 2) + amount-multiplier * amount`<br><br>
   * ... where the middle field can be truncated into:<br><br>
   * `shots-multplier * shots`<br><br>
   * ... without the natural logarithm exponent defined.
   * Limits can be applied to the number of shots and/or to the amount,
   * which will either zero the calculations or cap the results.
   * @param event identification for the event calculation parameters
   * @param weaponStat base weapon stat entry to be modified
   * @param canNotFindEventDefaultValue custom default value
   * @return weapon stat entry with a modified for the experience
   */
  private[exp] def calculateSupportExperience(
                                               event: String,
                                               weaponStat: WeaponStats,
                                               canNotFindEventDefaultValue: Option[Float] = None
                                             ): WeaponStats = {
    val rewards: Float = Config.app.game.experience.sep.events
      .find(evt => event.equals(evt.name))
      .map { event =>
        val shots = weaponStat.shots
        val shotsMax = event.shotsMax
        val shotsMultiplier = event.shotsMultiplier
        if (shots < event.shotsCutoff) {
          if (shotsMultiplier > 0f) {
            val modifiedShotsReward: Float = shotsMultiplier * math.log(math.min(shotsMax, shots).toDouble + 2d).toFloat
            val modifiedAmountReward: Float = event.amountMultiplier * weaponStat.amount.toFloat
            event.base.toFloat + modifiedShotsReward + modifiedAmountReward
          } else {
            event.base.toFloat
          }
        } else {
          0f
        }
      }
      .getOrElse(
        canNotFindEventDefaultValue.getOrElse(Config.app.game.experience.sep.canNotFindEventDefaultValue.toFloat)
      )
    weaponStat.copy(contributions = rewards)
  }
}
