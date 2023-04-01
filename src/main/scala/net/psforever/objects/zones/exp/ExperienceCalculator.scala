// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.{Default, PlanetSideGameObject}
import net.psforever.objects.avatar.scoring.{Assist, Death, Kill}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, SourceUniqueness, SourceWithHealthEntry}
import net.psforever.objects.vital.{DamageFromExplodingEntity, InGameActivity, InGameHistory}
import net.psforever.objects.vital.interaction.{Adversarial, DamageResult}
import net.psforever.objects.zones.Zone
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.collection.mutable
import scala.concurrent.duration._

object ExperienceCalculator {
  def apply(zone: Zone): Behavior[Command] =
    Behaviors.supervise[Command] {
      Behaviors.setup(context => new ExperienceCalculator(context, zone))
    }.onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class RewardThisDeath(victim: SourceEntry, lastDamage: Option[DamageResult], history: Iterable[InGameActivity])
    extends ExperienceCalculator.Command

  object RewardThisDeath {
    def apply(obj: PlanetSideGameObject with FactionAffinity with InGameHistory): RewardThisDeath = {
      RewardThisDeath(SourceEntry(obj), obj.LastDamage, obj.History)
    }
  }

  final case class RewardOurSupporters(target: SourceEntry, history: Iterable[InGameActivity], kill: Kill, bep: Long) extends Command

  object RewardOurSupporters {
    def apply(obj: PlanetSideGameObject with FactionAffinity with InGameHistory, kill: Kill): RewardOurSupporters = {
      RewardOurSupporters(SourceEntry(obj), obj.History, kill, kill.experienceEarned)
    }
  }

  private case object AssistDecay extends Command

  def calculateExperience(
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
    } else if (victim.Seated || victim.kills.nonEmpty) {
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
}

sealed case class InheritedAssistEntry(assists: Iterable[ContributionStatsOutput]) {
  val time: Long = System.currentTimeMillis()
}

class ExperienceCalculator(context: ActorContext[ExperienceCalculator.Command], zone: Zone)
  extends AbstractBehavior[ExperienceCalculator.Command](context) {

  import ExperienceCalculator._

  private val retainedKillAssists: mutable.HashMap[SourceUniqueness, InheritedAssistEntry] =
    mutable.HashMap[SourceUniqueness, InheritedAssistEntry]()

  private val retainedSupportAssists: mutable.HashMap[SourceUniqueness, InheritedAssistEntry] =
    mutable.HashMap[SourceUniqueness, InheritedAssistEntry]()

  private var inheritedAssistsDecayTimer: Cancellable = Default.Cancellable

  def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RewardThisDeath(victim: PlayerSource, lastDamage, history) =>
        rewardThisPlayerDeath(victim, lastDamage, history)

      case RewardThisDeath(victim: SourceWithHealthEntry, lastDamage, history) =>
        sortThisContributionForLater(victim, lastDamage, history)

      case RewardOurSupporters(target: PlayerSource, history, kill, bep) =>
        rewardTheseSupporters(target, history.toList, kill, bep)

      case RewardOurSupporters(target: SourceWithHealthEntry, history, kill, _) =>
        sortTheseSupportersForLater(target, history, kill)

      case AssistDecay =>
        performAssistDecay()

      case _ => ()
    }
    Behaviors.same
  }

  private def rewardThisPlayerDeath(
                                     victim: PlayerSource,
                                     lastDamage: Option[DamageResult],
                                     history: Iterable[InGameActivity]
                                   ): Unit = {
    val shortHistory = Support.limitHistoryToThisLife(history.toList)
    val everyone = KillDeathAssists.determineKiller(lastDamage, shortHistory) match {
      case Some((result, killer: PlayerSource)) =>
        val assists = collectAssistsForPlayer(victim, shortHistory, Some(killer))
        val fullBep = KillDeathAssists.calculateExperience(killer, victim, shortHistory)
        val hitSquad = (killer, Kill(victim, result, fullBep)) +: assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, shortHistory.last.time - shortHistory.head.time, fullBep)) +: hitSquad

      case _ =>
        val assists = collectAssistsForPlayer(victim, shortHistory, None)
        val fullBep = ExperienceCalculator.calculateExperience(victim, shortHistory)
        val hitSquad = assists.map {
          case ContributionStatsOutput(p, w, r) => (p, Assist(victim, w, r, (fullBep * r).toLong))
        }.toSeq
        (victim, Death(hitSquad.map { _._1 }, shortHistory.last.time - shortHistory.head.time, fullBep)) +: hitSquad
    }
    val events = zone.AvatarEvents
    everyone.foreach { case (p, kda) =>
      events ! AvatarServiceMessage(p.Name, AvatarAction.UpdateKillsDeathsAssists(p.CharId, kda))
    }
  }

  private[exp] def collectAssistsForPlayer(
                                            victim: PlayerSource,
                                            history: List[InGameActivity],
                                            killerOpt: Option[PlayerSource]
                                          ): Iterable[ContributionStatsOutput] = {
    val healthAssists = Support.collectHealthAssists(
      victim,
      history,
      Support.allocateContributors(KillDeathAssists.healthDamageContributors)
    )
    healthAssists.remove(0L)
//    if (armor > 0) {
//      val armorAssists = collectMaxArmorAssists(victim, history, armor.toFloat)
//    }
//    if (Support.wasEverAMax(victim, history)) {
//
//    } else {
//
//    }
    killerOpt.map { killer => healthAssists.remove(killer.CharId) }
    healthAssists.values
  }

  private def rewardTheseSupporters(
                                     target: SourceEntry,
                                     history: List[InGameActivity],
                                     kill: Kill,
                                     bep: Long
                                   ): Unit = {
    val time = kill.time
    val events = zone.AvatarEvents
    val trimmedHistory = Support.limitHistoryToThisLife(history)
    val normalAssists = Support.onlyOriginalAssistEntries(
      Support.collectHealingSupportAssists(target, time, trimmedHistory),
      Support.collectRepairingSupportAssists(target, time, trimmedHistory)
    )
    (retainedSupportAssists.get(target.unique) match {
      case Some(support) =>
        Support.onlyOriginalAssistEntriesIterable(normalAssists, support.assists)
      case None =>
        normalAssists
    })
      .foreach { case ContributionStatsOutput(p, _, ratio) =>
        events ! AvatarServiceMessage(p.Name, AvatarAction.AwardSupportBep(p.CharId, (ratio * bep).toLong))
      }
    Support.collectTerminalSupportAssists(target, history).foreach { case ContributionStatsOutput(p, _, reward) =>
      events ! AvatarServiceMessage(p.Name, AvatarAction.AwardSupportBep(p.CharId, reward.toLong))
    }
  }

  /* */

  private def sortTheseSupportersForLater(
                                           target: SourceEntry,
                                           history: Iterable[InGameActivity],
                                           kill: Kill
                                         ): Unit = {
    val time = kill.time
    val trimmedHistory = Support.limitHistoryToThisLife(history.toList)
    val assists = Support.collectRepairingSupportAssists(target, time, trimmedHistory).values
    retainAssistsAndScheduleBlanking(target.unique, assists)
  }

  private def sortThisContributionForLater(
                                            target: SourceWithHealthEntry,
                                            lastDamage: Option[DamageResult],
                                            history: Iterable[InGameActivity]
                                          ): Unit = {
    val shortHistory = Support.limitHistoryToThisLife(history.toList)
    val assists = KillDeathAssists.determineKiller(lastDamage, shortHistory) match {
      case Some((damage, killer: PlayerSource)) =>
        ContributionStatsOutput(killer, Seq(damage.interaction.cause.attribution), 1f) +:
          collectRetainableAssistsForEntity(target, shortHistory, Some(killer)).toSeq
      case _ =>
        collectRetainableAssistsForEntity(target, shortHistory, None)
    }
    retainAssistsAndScheduleBlanking(target.unique, assists)
  }

  private def collectRetainableAssistsForEntity(
                                                 entity: SourceWithHealthEntry,
                                                 history: List[InGameActivity],
                                                 killerOpt: Option[PlayerSource]
                                               ): Iterable[ContributionStatsOutput] = {
    val assists = KillDeathAssists.collectAssistsForEntity(entity, history, killerOpt)
    Support.onlyOriginalAssistEntriesIterable(
      assists,
      recoverRetainedKillAssists(assists, history)
    )
  }

  private def recoverRetainedKillAssists(
                                  assists: Iterable[ContributionStatsOutput],
                                  history: Iterable[InGameActivity]
                                ): Iterable[ContributionStatsOutput] = {
    history.collect { case ex: DamageFromExplodingEntity =>
      ex.data
        .adversarial
        .flatMap {
          case Adversarial(attacker: PlayerSource, _, _) =>
            Some((
              assists.find { case ContributionStatsOutput(p, _, _) => p == attacker },
              retainedKillAssists.get(ex.data.targetAfter.unique)
            ))
        }
        .map {
          case (Some(_), Some(retained)) => retained.assists
        }
    }.flatten.flatten
  }

  private[this] def retainAssistsAndScheduleBlanking(
                                                      target: SourceUniqueness,
                                                      assists: Iterable[ContributionStatsOutput]
                                                    ): Boolean = {
    val retime = retainedKillAssists.isEmpty && inheritedAssistsDecayTimer.isCancelled
    retainedKillAssists.put(target, InheritedAssistEntry(assists))
    if (retime) {
      inheritedAssistsDecayTimer = context.scheduleOnce(3.minutes, context.self, AssistDecay)
    }
    retime
  }

  private[this] def performAssistDecay(): Unit = {
    val curr = System.currentTimeMillis()
    val dur = 5.minutes.toMillis
    retainedKillAssists.filterInPlace { case (_, entry) => curr - entry.time < dur }
    retainedSupportAssists.filterInPlace { case (_, entry) => curr - entry.time < dur }
    if (retainedKillAssists.nonEmpty || retainedSupportAssists.nonEmpty) {
      inheritedAssistsDecayTimer = context.scheduleOnce(3.minutes, context.self, AssistDecay)
    } else {
      inheritedAssistsDecayTimer = Default.Cancellable
    }
  }
}
