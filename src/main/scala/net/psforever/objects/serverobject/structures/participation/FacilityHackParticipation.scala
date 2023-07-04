// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.Player
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.sourcing.{PlayerSource, UniquePlayer}
import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.collection.mutable

trait FacilityHackParticipation extends ParticipationLogic {
  protected var lastInfoRequest: Long = 0L
  protected var infoRequestOverTime: Seq[Long] = Seq[Long]()
  /*
  key: unique player identifier
   */
  protected var playerContribution: mutable.LongMap[(Player, Int, Long)] = mutable.LongMap[(Player, Int, Long)]()
  protected var playerPopulationOverTime: Seq[Map[PlanetSideEmpire.Value, Int]] = Seq[ Map[PlanetSideEmpire.Value, Int]]()

  protected def updatePlayers(list: List[Player]): Unit = {
    val hackTime = building.CaptureTerminal.get.Definition.FacilityHackTime.toMillis
    val curr = System.currentTimeMillis()
    if (list.isEmpty) {
      playerContribution = playerContribution.filterNot { case (_, (_, _, t)) => curr - t > hackTime }
    } else {
      val (vanguardParticipants, missingParticipants) = {
        val uniqueList2 = list.map { _.CharId }
        playerContribution
          .filterNot { case (_, (_, _, t)) => curr - t > hackTime }
          .partition { case (p, _) => uniqueList2.contains(p) }
      }
      val newParticipaants = list
        .filterNot { p =>
          playerContribution.exists { case (u, _) => p.CharId == u }
        }
      playerContribution =
        vanguardParticipants.map { case (u, (p, d, _)) => (u, (p, d + 1, curr)) } ++
          newParticipaants.map { p => (p.CharId, (p, 1, curr)) } ++
          missingParticipants
    }
  }

  protected def updatePopulationOverTime(list: List[Player], now: Long, before: Long): Unit = {
    var populationList = list
    val layer = PlanetSideEmpire.values.map { faction =>
      val (isFaction, everyoneElse) = populationList.partition(_.Faction == faction)
      populationList = everyoneElse
      (faction, isFaction.size)
    }.toMap[PlanetSideEmpire.Value, Int]
    playerPopulationOverTime = timeSensitiveFilterAndAppend(playerPopulationOverTime, layer, now, before)
  }

  protected def timeSensitiveFilterAndAppend[T](
                                                 list: Seq[T],
                                                 newEntry: T,
                                                 now: Long = System.currentTimeMillis(),
                                                 before: Long
                                               ): Seq[T] = {
    infoRequestOverTime match {
      case Nil =>
        Seq(newEntry)
      case _ =>
        val beforeTime = now - before
        (infoRequestOverTime.indexWhere { _ >= beforeTime } match {
          case -1 =>
            list
          case cutOffIndex =>
            list.drop(cutOffIndex)
        }) :+ newEntry
    }
  }
}

object FacilityHackParticipation {
  private[participation] def calculateExperienceFromKills(
                                                           center: Vector3,
                                                           radius: Float,
                                                           hackStart: Long,
                                                           completionTime: Long,
                                                           opposingFaction: PlanetSideEmpire.Value,
                                                           contributionVictor: Iterable[(Player, Int, Long)],
                                                           contributionOpposingSize: Int
                                                         ): Long = {
    val killMapFunc: Iterable[(Player, Int, Long)] => Iterable[(UniquePlayer, Float, Seq[Kill])] = {
        killsEarnedPerPlayerDuringHack(center.xy, radius * radius, hackStart, hackStart + completionTime, opposingFaction)
    }
    val killMapValues = killMapFunc(contributionVictor)
    val totalExperienceFromKills = killMapValues.flatMap { _._3.map { _.experienceEarned } }.sum
    val experienceModifier = {
      if (contributionOpposingSize > 0 && contributionOpposingSize < 10) {
        contributionOpposingSize * 0.1f + math.random()
      } else {
        contributionOpposingSize * 0.1f
      }
    }
    (totalExperienceFromKills * experienceModifier).toLong
  }

  private def killsEarnedPerPlayerDuringHack(
                                              centerXY: Vector3,
                                              distanceSq: Float,
                                              start: Long,
                                              end: Long,
                                              faction: PlanetSideEmpire.Value
                                            )
                                            (
                                              list: Iterable[(Player, Int, Long)]
                                            ): Iterable[(UniquePlayer, Float, Seq[Kill])] = {
    val duration = end - start
    list.map { case (p, d, _) =>
      val killList = p.avatar.scorecard.Kills.filter { k =>
        val killTime = k.info.interaction.hitTime
        k.victim.Faction == faction && start < killTime && killTime < end && Vector3.DistanceSquared(centerXY, k.info.interaction.hitPos.xy) < distanceSq
      }
      (PlayerSource(p).unique, math.min(d, duration).toFloat / duration.toFloat, killList)
    }
  }

  private[participation] def diffHeatForFactionMap(
                                                    data: mutable.HashMap[Vector3, Map[PlanetSideEmpire.Value, Seq[Long]]],
                                                    faction: PlanetSideEmpire.Value
                                                  ): Map[Vector3, Seq[Long]] = {
    var lastHeatAmount: Long = 0
    var outList: Seq[Long] = Seq[Long]()
    data.map { case (key, map) =>
      map(faction) match {
        case Nil => ()
        case value :: Nil =>
          outList = outList :+ value
        case value :: list =>
          lastHeatAmount = value
          list.foreach { heat =>
            if (heat < lastHeatAmount) {
              lastHeatAmount = heat
              outList = outList :+ heat
            } else {
              outList = outList :+ (heat - lastHeatAmount)
              lastHeatAmount = heat
            }
          }
      }
      (key, outList)
    }.toMap[Vector3, Seq[Long]]
  }

  private[participation] def heatMapComparison(
                                                victorData: Iterable[Seq[Long]],
                                                opposedData: Iterable[Seq[Long]]
                                              ): Float = {
    var dataCount: Int = 0
    var dataSum: Float = 0
    if (victorData.size == opposedData.size) {
      val seq1 = victorData.toSeq
      val seq2 = opposedData.toSeq
      seq1.indices.foreach { outerIndex =>
        val list1 = seq1(outerIndex)
        val list2 = seq2(outerIndex)
        if (list1.size == list2.size) {
          val indices1 = list1.indices
          dataCount = dataCount + indices1.size
          indices1.foreach { innerIndex =>
            val value1 = list1(innerIndex)
            val value2 = list2(innerIndex)
            if (value1 * value2 == 0) {
              dataCount -= 1
            } else if (value1 > value2) {
              dataSum = dataSum - value2.toFloat / value1.toFloat
            } else {
              dataSum = dataSum + value2.toFloat / value1.toFloat
            }
          }
        }
      }
    }
    if (dataSum != 0) {
      math.max(0.15f, math.min(2f, dataSum / dataCount.toFloat))
    } else {
      1f //can't do anything; multiplier should not affect values
    }
  }

  /**
   * na
   * @param populationNumbers list of the population updates
   * @param gradingRule the rule whereby population numbers are transformed into percentage bonus
   * @param layers from largest groupings of percentages from applying the above rule, average the values from this many groups
   * @return the modifier value
   */
  private[participation] def populationProgressModifier(
                                                         populationNumbers: Seq[Int],
                                                         gradingRule: Int=>Float,
                                                         layers: Int
                                                       ): Float = {
    val gradedPopulation = populationNumbers
      .map { gradingRule }
      .groupBy(x => x)
      .values
      .toSeq
      .sortBy(_.size)
      .take(layers)
      .flatten
    gradedPopulation.sum / gradedPopulation.size.toFloat
  }

  private[participation] def populationBalanceModifier(
                                                        victorPopulationNumbers: Seq[Int],
                                                        opposingPopulationNumbers: Seq[Int],
                                                        healthyPercentage: Float
                                                      ): Float = {
    val rate = for {
      victorPop <- victorPopulationNumbers
      opposePop <- opposingPopulationNumbers
      out = if (
        (opposePop < victorPop && opposePop * healthyPercentage > victorPop) ||
          (opposePop > victorPop && victorPop * healthyPercentage > opposePop)
      ) {
        1f //balanced enough population
      } else {
        opposePop / victorPop.toFloat
      }
      if true
    } yield out
    rate.sum / rate.size.toFloat
  }

  private[participation] def competitionBonus(
                                               victorSize: Long,
                                               opposingSize: Long,
                                               steamrollPercentage: Float,
                                               steamrollBonus: Long,
                                               overwhelmingOddsPercentage: Float,
                                               overwhelmingOddsBonus: Long
                                             ): Long = {
    if (opposingSize * steamrollPercentage < victorSize.toFloat) {
      -steamrollBonus * (victorSize - opposingSize) //steamroll by the victor
    } else if (victorSize * overwhelmingOddsPercentage <= opposingSize.toFloat) {
      overwhelmingOddsBonus + opposingSize + victorSize //victory against overwhelming odds
    } else {
      steamrollBonus * opposingSize //still a battle
    }
  }
}
