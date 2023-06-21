// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import net.psforever.objects.PlanetSideGameObject
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
}

class ExperienceCalculator(context: ActorContext[ExperienceCalculator.Command], zone: Zone)
  extends AbstractBehavior[ExperienceCalculator.Command](context) {

  import ExperienceCalculator._

  def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RewardThisDeath(victim: PlayerSource, lastDamage, history) =>
        KillAssists.rewardThisPlayerDeath(victim, lastDamage, history, zone.AvatarEvents)

      case _ => ()
    }
    Behaviors.same
  }
}
