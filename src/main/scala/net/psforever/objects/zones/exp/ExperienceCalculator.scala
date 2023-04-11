// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.{InGameActivity, InGameHistory}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone

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

  def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RewardThisDeath(victim: PlayerSource, lastDamage, history) =>
        KillAssists.rewardThisPlayerDeath(victim, lastDamage, history, zone.AvatarEvents)

      case RewardOurSupporters(target: PlayerSource, history, kill, bep) =>
        KillContributions.rewardTheseSupporters(target, history, kill, bep, zone.AvatarEvents)

      case _ => ()
    }
    Behaviors.same
  }
}
