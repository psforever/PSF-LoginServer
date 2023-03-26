// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.GlobalDefinitions

import java.util.Date
import org.joda.time.{Instant, LocalDateTime => JodaLocalDateTime}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource, SourceEntry}
import net.psforever.objects.vital.{HealFromEquipment, InGameActivity, RepairFromEquipment, RevivingActivity, SpawningActivity, SupportActivityCausedByAnother, TerminalUsedActivity}
import net.psforever.objects.zones.Zone
import net.psforever.types.TransactionType
import net.psforever.zones.Zones

import scala.collection.mutable

object Support {
  private type SupportActivity = InGameActivity with SupportActivityCausedByAnother

  private[exp] def collectHealingSupportAssists(
                                                 target: SourceEntry,
                                                 time: JodaLocalDateTime,
                                                 history: Iterable[InGameActivity]
                                               ): mutable.LongMap[ContributionStatsOutput] = {
    //normal heals
    val healInfo = collectSupportContributions(
      target, time, ExperienceCalculator.limitHistoryToThisLife(history.toList).collect { case heals: HealFromEquipment
        if heals.amount > 0 && heals.user.unique != target.unique => (heals, heals.equipment_def.ObjectId)
      }, mapContributionPointsByPercentage
    )
    //revivals (count all revivals performed by the player of the last one, if any)
    val reviveInfo = collectSupportContributions(
      target, time, history.collect { case revive: RevivingActivity
        if revive.user.unique != target.unique => (revive, revive.equipment.ObjectId)
      }.lastOption.toSeq, mapContributionPointsByCount
    )
    //combine and output
    reviveInfo.foreach { case (reviverId, contribution) =>
      val result = healInfo.get(reviverId)
      val percentage = result.map { a => a.percentage }.getOrElse { contribution.percentage }
      val implements = result.map { a => (a.implements ++ contribution.implements).distinct }.getOrElse { contribution.implements }
      healInfo.put(reviverId, ContributionStatsOutput(contribution.player, implements, percentage))
    }
    healInfo
  }

  private[exp] def collectRepairingSupportAssists(
                                                 target: SourceEntry,
                                                 time: JodaLocalDateTime,
                                                 history: Iterable[InGameActivity]
                                               ): mutable.LongMap[ContributionStatsOutput] = {
    collectSupportContributions(
      target, time, ExperienceCalculator.limitHistoryToThisLife(history.toList).collect { case repairs: RepairFromEquipment
        if repairs.amount > 0 && repairs.user.unique != target.unique => (repairs, repairs.equipment_def.ObjectId)
      }, mapContributionPointsByPercentage
    )
  }

  private[exp] def collectTerminalSupportAssists(
                                                  target: SourceEntry,
                                                  history: List[InGameActivity]
                                                ): Iterable[ContributionStatsOutput] = {
    val delay: Long = 300000L
    val curr = System.currentTimeMillis()
    var zone: Zone = Zone.Nowhere
    val termsUsed: mutable.LongMap[Option[AmenitySource]] = mutable.LongMap[Option[AmenitySource]]()
    val credit: mutable.LongMap[ContributionStatsOutput] = mutable.LongMap[ContributionStatsOutput]()
    val faction = target.Faction
    val terminalUseHistory = history.filter {
      case _: SpawningActivity         => true
      case entry: TerminalUsedActivity => curr - entry.time < delay
      case _                           => false
    }
    terminalUseHistory.collect {
      case entry: SpawningActivity =>
        zone = Zones.zones(entry.zoneNumber)
      case entry: TerminalUsedActivity
        if !termsUsed.contains(entry.terminal.unique.guid.guid.toLong) =>
        if (
          entry.transaction == TransactionType.Loadout ||
          entry.transaction == TransactionType.Buy ||
          entry.transaction == TransactionType.Learn
        ) {
          val terminal = entry.terminal
          val guid = terminal.unique.guid.guid
          terminal.hacked.collect {
            case Hackable.HackInfo(player, _, _, _)
              if target.CharId != player.CharId && faction == player.Faction =>
              //accessed a hacked terminal
              termsUsed.put(guid.toLong, Some(terminal))
              addTerminalContributionEntry(credit, player, Seq(GlobalDefinitions.remote_electronics_kit.ObjectId), percentage = 1f)
            case _
              if terminal.Faction == faction =>
              //accessed a faction-friendly terminal; check log for repair history
              termsUsed.put(guid.toLong, Some(terminal))
              zone.GUID(guid)
                .asInstanceOf[Terminal]
                .History
                .collect { case a: RepairFromEquipment =>
                  val user = a.user
                  addTerminalContributionEntry(credit, user, Seq(a.equipment_def.ObjectId), percentage = 0.5f)
                }
            case _ =>
              //what is this?
              termsUsed.put(guid.toLong, None)
          }
        }
    }
    credit.remove(target.CharId)
    credit.values
  }

  private def addTerminalContributionEntry(
                                            contributions: mutable.LongMap[ContributionStatsOutput],
                                            player: PlayerSource,
                                            implements: Seq[Int],
                                            percentage: Float
                                          ): Unit = {
    val charId = player.CharId
    contributions.get(charId) match {
      case Some(entry) if percentage > entry.percentage =>
        contributions.put(charId, entry.copy(percentage = percentage))
      case Some(entry) =>
        contributions.put(charId, entry.copy(
          implements = (entry.implements ++ implements).distinct,
          percentage = entry.percentage + 0.05f
        ))
      case None =>
        contributions.put(charId, ContributionStatsOutput(player, implements, percentage))
    }
  }

  private def findTimeApplicableActivities(
                                            activity: Seq[SupportActivity],
                                            killLastTime: JodaLocalDateTime,
                                            timeOffset: Int //s
                                          ): Seq[SupportActivity] = {
    val dateTimeConverter: Date=>JodaLocalDateTime = JodaLocalDateTime.fromDateFields
    val milliToInstant: Long=>org.joda.time.Instant = Instant.ofEpochMilli
    val targetTime = killLastTime.minusSeconds(timeOffset)
    //find all activities that occurred after the kill time after the offset is considered
    activity
      .filter { a =>
        val activityTime = dateTimeConverter(milliToInstant(a.time).toDate)
        targetTime.isBefore(activityTime) || targetTime.equals(activityTime)
      }
      .sortBy(_.time)(Ordering.Long.reverse)
  }

  private def allocateSupportAssists(
                                      longTerm: Seq[SupportActivity],
                                      shortTerm: Seq[SupportActivity],
                                      defaultTool: Int
                                    ): mutable.LongMap[ContributionStats] = {
    val contributions: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
    (longTerm ++ shortTerm).foreach { h =>
      val amount = h.amount
      val user = h.user
      val charId = user.CharId
      contributions.get(charId) match {
        case Some(entry) =>
          contributions.update(charId, entry.copy(
            amount = entry.amount + amount,
            total = entry.total + amount,
            shots = entry.shots + 1,
            time = math.max(entry.time, h.time)
          ))
        case None =>
          contributions.put(charId, ContributionStats(user, Seq(WeaponStats(defaultTool, amount, 1, h.time)), amount, amount, 1, h.time))
      }
    }
    contributions
  }

  private def collectSupportContributions(
                                           target: SourceEntry,
                                           time: JodaLocalDateTime,
                                           pairs: Seq[(InGameActivity, Int)],
                                           contributionPointsMap: (Float,Iterable[SupportActivity])=>(Long,ContributionStats)=>(Long,ContributionStatsOutput)
                                         ): mutable.LongMap[ContributionStatsOutput] = {
    val (activity, tools) = pairs.unzip
    //TODO this is not correct, but it will do for now
    if (tools.isEmpty) {
      mutable.LongMap[ContributionStatsOutput]()
    } else if (tools.distinct.size == 1) {
      collectSupportAssists(target, time, activity, tools.head, contributionPointsMap)
    } else {
      var output = Iterable[ContributionStatsOutput]()
      tools.groupBy(identity).keys.foreach { implement =>
        output = ExperienceCalculator.onlyOriginalAssistEntriesIterable(
          output,
          collectSupportAssists(target, time, activity, implement, contributionPointsMap).values
        )
      }
      mutable.LongMap[ContributionStatsOutput]().addAll(output.map { entry => (entry.player.CharId, entry) })
    }
  }

  private def collectSupportAssists(
                                     target: SourceEntry,
                                     killLastTime: JodaLocalDateTime,
                                     activity: Seq[InGameActivity],
                                     defaultImplement: Int,
                                     contributionPointsMap: (Float,Iterable[SupportActivity])=>(Long,ContributionStats)=>(Long,ContributionStatsOutput)
                                   ): mutable.LongMap[ContributionStatsOutput] = {
    val activityByAnother = activity.collect {
      case activity: SupportActivity => activity
    }
    val longTermActivity = findTimeApplicableActivities(activityByAnother, killLastTime, timeOffset = 600)
    val shortTermActivity = findTimeApplicableActivities(activityByAnother, killLastTime, timeOffset = 300)
    val contributions = allocateSupportAssists(longTermActivity, shortTermActivity, defaultImplement)
    contributions.remove(target.CharId)
    val mapFunc = contributionPointsMap(contributions.values.foldLeft(0)(_ + _.total).toFloat, shortTermActivity)
    contributions.map { case (a, b) => mapFunc(a, b) }
  }

  private def mapContributionPointsByPercentage(
                                                 total: Float,
                                                 compareList: Iterable[SupportActivity]
                                               )
                                               (
                                                 charId: Long,
                                                 contribution: ContributionStats,
                                               ): (Long, ContributionStatsOutput) = {
    val user = contribution.player
    val unique = user.unique
    val points = contribution.amount
    val value = if (points < 75) {
      //a small contribution means the lower time limit
      if (compareList.exists { a => a.user.unique == unique }) {
        math.max(0.2f, points / total)
      } else {
        0f
      }
    } else {
      //large contribution is always okay
      if (points > 299) {
        1.0f
      } else if (points > 100) {
        0.75f
      } else {
        0.5f
      }
    }
    (charId, ContributionStatsOutput(user, contribution.weapons.map { _.weapon_id }, value))
  }

  //noinspection ScalaUnusedSymbol
  private def mapContributionPointsByCount(
                                            total: Float,
                                            compareList: Iterable[SupportActivity]
                                          )
                                          (
                                            charId: Long,
                                            contribution: ContributionStats,
                                          ): (Long, ContributionStatsOutput) = {
    (charId, ContributionStatsOutput(contribution.player, contribution.weapons.map { _.weapon_id }, compareList.size.toFloat))
  }
}
