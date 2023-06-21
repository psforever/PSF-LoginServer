// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.{InGameActivity, InGameHistory}
import net.psforever.objects.zones.Zone

object SupportExperienceCalculator {
  def apply(zone: Zone): Behavior[Command] =
    Behaviors.supervise[Command] {
      Behaviors.setup(context => new SupportExperienceCalculator(context, zone))
    }.onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class RewardOurSupporters(target: SourceEntry, history: Iterable[InGameActivity], kill: Kill, bep: Long) extends Command

  object RewardOurSupporters {
    def apply(obj: PlanetSideGameObject with FactionAffinity with InGameHistory, kill: Kill): RewardOurSupporters = {
      RewardOurSupporters(SourceEntry(obj), obj.History, kill, kill.experienceEarned)
    }
  }
}

class SupportExperienceCalculator(context: ActorContext[SupportExperienceCalculator.Command], zone: Zone)
  extends AbstractBehavior[SupportExperienceCalculator.Command](context) {

  import SupportExperienceCalculator._

  def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RewardOurSupporters(target: PlayerSource, history, kill, bep) =>
        KillContributions.rewardTheseSupporters(target, history, kill, bep, zone.AvatarEvents)

      case _ => ()
    }
    Behaviors.same
  }
}
