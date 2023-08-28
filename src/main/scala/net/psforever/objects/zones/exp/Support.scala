// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.GlobalDefinitions

import java.util.Date
import org.joda.time.{Instant, LocalDateTime => JodaLocalDateTime}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource, SourceEntry}
import net.psforever.objects.vital.{HealFromEquipment, InGameActivity, ReconstructionActivity, RepairFromEquipment, RepairFromExoSuitChange, RevivingActivity, SpawningActivity, SupportActivityCausedByAnother, TerminalUsedActivity}
import net.psforever.objects.zones.Zone
import net.psforever.types.{ExoSuitType, PlanetSideEmpire, TransactionType}
import net.psforever.zones.Zones

import scala.collection.mutable

object Support {
  private type SupportActivity = InGameActivity with SupportActivityCausedByAnother

  private[exp] def baseExperience(
                                   victim: PlayerSource,
                                   history: Iterable[InGameActivity]
                                 ): Long = {
    val lifespan = (history.headOption, history.lastOption) match {
      case (Some(spawn), Some(death)) => death.time - spawn.time
      case _                          => 0L
    }
    val wasEverAMax = Support.wasEverAMax(victim, history)
    val base = if (wasEverAMax) { //shamed
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

  private[exp] def onlyOriginalAssistEntries(
                                              first: mutable.LongMap[ContributionStatsOutput],
                                              second: mutable.LongMap[ContributionStatsOutput],
                                              combiner: (ContributionStatsOutput, ContributionStatsOutput)=>ContributionStatsOutput =
                                              defaultAdditiveOutputCombiner(multiplier = 0.05f)
                                            ): Iterable[ContributionStatsOutput] = {
    onlyOriginalAssistEntriesIterable(first.values, second.values, combiner)
  }

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

  private[exp] def collectHealingSupportAssists(
                                                 target: SourceEntry,
                                                 time: JodaLocalDateTime,
                                                 history: List[InGameActivity]
                                               ): mutable.LongMap[ContributionStatsOutput] = {
    //normal heals
    val healInfo = collectSupportContributions(
      target, time, history.collect { case heals: HealFromEquipment
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
                                                 history: List[InGameActivity]
                                               ): mutable.LongMap[ContributionStatsOutput] = {
    collectSupportContributions(
      target,
      time,
      history.collect { case repairs: RepairFromEquipment
        if repairs.amount > 0 && repairs.user.unique != target.unique => (repairs, repairs.equipment_def.ObjectId)
      },
      mapContributionPointsByPercentage
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
              addTerminalContributionEntry(credit, player, Seq(HackKillAssist(GlobalDefinitions.remote_electronics_kit.ObjectId)), percentage = 1f)
            case _
              if terminal.Faction == faction =>
              //accessed a faction-friendly terminal; check log for repair history
              termsUsed.put(guid.toLong, Some(terminal))
              zone.GUID(guid)
                .asInstanceOf[Terminal]
                .History
                .collect { case a: RepairFromEquipment =>
                  val user = a.user
                  //TODO might be wrong intermediate
                  addTerminalContributionEntry(credit, user, Seq(RepairKillAssist(a.equipment_def.ObjectId, target.Definition.ObjectId)), percentage = 0.5f)
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
                                            implements: Seq[EquipmentUseContextWrapper],
                                            percentage: Float
                                          ): Unit = {
    val charId = player.CharId
    contributions.get(charId) match {
      case Some(entry) if percentage > entry.percentage =>
        contributions.put(charId, entry.copy(percentage = percentage))
      case Some(entry) =>
        contributions.put(charId, entry.copy(implements = (entry.implements ++ implements).distinct, percentage = entry.percentage + 0.05f))
      case None =>
        contributions.put(charId, ContributionStatsOutput(player, implements, percentage))
    }
  }

  private[exp] def findTimeApplicableActivities(
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

  private def combineTimeApplicableActivitiesForContribution(
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
          //the contribution percentage allocated will always be 1.0f and should be overwritten later
          contributions.put(charId, ContributionStats(user, Seq(WeaponStats(NoUse(defaultTool), amount, 1, h.time, 1f)), amount, amount, 1, h.time))
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
    } else {
      val distinctTools = tools.distinct
      if (distinctTools.size == 1) {
        compileTimedSupportContributions(target, time, activity, distinctTools.head, contributionPointsMap)
      } else {
        var output = Iterable[ContributionStatsOutput]()
        distinctTools.foreach { implement =>
          output = onlyOriginalAssistEntriesIterable(
            output,
            compileTimedSupportContributions(target, time, activity, implement, contributionPointsMap).values
          )
        }
        mutable.LongMap[ContributionStatsOutput]().addAll(output.map { entry => (entry.player.CharId, entry) })
      }
    }
  }

  private def compileTimedSupportContributions(
                                                target: SourceEntry,
                                                timeLimit: JodaLocalDateTime,
                                                activity: Seq[InGameActivity],
                                                defaultImplement: Int,
                                                contributionPointsMap: (Float,Iterable[SupportActivity])=>(Long,ContributionStats)=>(Long,ContributionStatsOutput)
                                              ): mutable.LongMap[ContributionStatsOutput] = {
    val activityByAnother = activity.collect { case theActivity: SupportActivity => theActivity }
    val longTermActivity = findTimeApplicableActivities(activityByAnother, timeLimit, timeOffset = 600)
    val shortTermActivity = findTimeApplicableActivities(activityByAnother, timeLimit, timeOffset = 300)
    val contributions = combineTimeApplicableActivitiesForContribution(longTermActivity, shortTermActivity, defaultImplement)
    contributions.remove(target.CharId)
    val mapFunc = contributionPointsMap(contributions.values.foldLeft(0)(_ + _.total).toFloat, shortTermActivity)
    contributions.map { case (a, b) => mapFunc(a, b) }
  }

  private[exp] def allocateContributors(
                                         tallyFunc: (List[InGameActivity], PlanetSideEmpire.Value, mutable.LongMap[ContributionStats]) => Any
                                       )
                                       (
                                         history: List[InGameActivity],
                                         faction: PlanetSideEmpire.Value
                                       ): mutable.LongMap[ContributionStats] = {
    /** players who have contributed to this death, and how much they have contributed<br>
     * key - character identifier,
     * value - (player, damage, total damage, number of shots) */
    val participants: mutable.LongMap[ContributionStats] = mutable.LongMap[ContributionStats]()
    tallyFunc(history, faction, participants)
    participants
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
    (charId, ContributionStatsOutput(user, contribution.weapons.map { _.equipment }, value))
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
    (charId, ContributionStatsOutput(contribution.player, contribution.weapons.map { _.equipment }, compareList.size.toFloat))
  }

  private[exp] def wasEverAMax(player: PlayerSource, history: Iterable[InGameActivity]): Boolean = {
    player.ExoSuit == ExoSuitType.MAX || history.exists {
      case SpawningActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case ReconstructionActivity(p: PlayerSource, _, _) => p.ExoSuit == ExoSuitType.MAX
      case RepairFromExoSuitChange(suit, _) => suit == ExoSuitType.MAX
      case _                                => false
    }
  }
}
