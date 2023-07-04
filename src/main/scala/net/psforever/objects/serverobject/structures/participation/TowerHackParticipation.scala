// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Config

final case class TowerHackParticipation(building: Building) extends FacilityHackParticipation {
  def TryUpdate(): Unit = {
    val list = building.PlayersInSOI
    updatePlayers(building.PlayersInSOI)
    val now = System.currentTimeMillis()
    if (now - lastInfoRequest > 60000L) {
      updatePopulationOverTime(list, now, before = 300000L)
    }
  }

  def RewardFacilityCapture(
                             defenderFaction: PlanetSideEmpire.Value,
                             attackingFaction: PlanetSideEmpire.Value,
                             hacker: PlayerSource,
                             hackTime: Long,
                             completionTime: Long,
                             isResecured: Boolean
                           ): Unit = {
    val curr = System.currentTimeMillis()
    val hackStart = curr - completionTime
    val (victorFaction, opposingFaction) = if (!isResecured) {
      (attackingFaction, defenderFaction)
    } else {
      (defenderFaction, attackingFaction)
    }
    val (contributionVictor, contributionOpposing, _) = {
      //TODO this is only to preserve a semblance of the original return type; fix this output
      val (a, b1) = playerContribution.partition { case (_, (p, _, _)) => p.Faction == victorFaction }
      val (b, c) = b1.partition { case (_, (p, _, _)) => p.Faction == opposingFaction }
      (a.values, b.values, c.values)
    }
    val contributionVictorSize = contributionVictor.size
    if (contributionVictorSize > 0) {
      val contributionOpposingSize = contributionOpposing.size
      //1) experience from killing opposingFaction across duration of hack
      val baseExperienceFromFacilityCapture: Long = FacilityHackParticipation.calculateExperienceFromKills(
        building.Position,
        building.Definition.SOIRadius.toFloat,
        hackStart,
        completionTime,
        opposingFaction,
        contributionVictor,
        contributionOpposingSize
      )
      //setup for ...
      val populationIndices = playerPopulationOverTime.indices
      val allFactions = PlanetSideEmpire.values.filterNot {
        _ == PlanetSideEmpire.NEUTRAL
      }.toSeq
      val (victorPopulationByLayer, opposingPopulationByLayer) = {
        val individualPopulationByLayer = allFactions.map { f =>
          (f, populationIndices.indices.map { i => playerPopulationOverTime(i)(f) })
        }.toMap[PlanetSideEmpire.Value, Seq[Int]]
        (individualPopulationByLayer(victorFaction), individualPopulationByLayer(opposingFaction))
      }
      //2) peak population modifier
      val populationModifier = FacilityHackParticipation.populationProgressModifier(
        opposingPopulationByLayer,
        { pop =>
          if (pop > 59) 0.75f
          else if (pop > 29) 0.675f
          else if (pop > 25) 0.55f
          else 0.5f
        },
        2
      )
      //3) competition bonus
      val competitionBonus: Long = FacilityHackParticipation.competitionBonus(
        contributionVictorSize,
        contributionOpposingSize,
        steamrollPercentage = 1.25f,
        steamrollBonus = 2L,
        overwhelmingOddsPercentage = 0.5f,
        overwhelmingOddsBonus = 30L
      )
      //4) competition multiplier
      val competitionMultiplier: Float = FacilityHackParticipation.populationBalanceModifier(
        victorPopulationByLayer,
        opposingPopulationByLayer,
        healthyPercentage = 1.25f
      )
      //calculate overall command experience points and the individual player multiplier
      val finalCep: Long = math.ceil(
        math.max(1L, baseExperienceFromFacilityCapture + competitionBonus) *
          populationModifier *
          competitionMultiplier *
          Config.app.game.cepRate
      ).toLong
      val contributionPerPlayerByTime = playerContribution.collect {
        case (a, (_, d, t)) if d >= 300000 && math.abs(completionTime - t) < 5000 =>
          (a, 0.5f)
        case (a, (_, d, t)) if math.abs(completionTime - t) < 5000 =>
          (a, 0.25f * (1f + (d.toFloat / 3000000f)))
        case (a, (_, d, t)) =>
          (a, 0.25f * (1f + (math.max(0, d - completionTime + t).toFloat / 3000000f)))
      }
      //reward participant(s)
      // classically, only players in the SOI are rewarded
      val events = building.Zone.AvatarEvents
      building.PlayersInSOI
        .filter { player =>
          player.Faction == victorFaction && player.CharId != hacker.CharId
        }
        .foreach { player =>
          val contributionMultiplier = contributionPerPlayerByTime.getOrElse(player.CharId, 1f)
          events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(0, (finalCep * contributionMultiplier).toLong))
        }
      events ! AvatarServiceMessage(hacker.Name, AvatarAction.AwardCep(hacker.CharId, finalCep))
    }

    playerContribution.clear()
    playerPopulationOverTime.reverse match {
      case entry :: _ => playerPopulationOverTime = Seq(entry)
    }
  }
}
