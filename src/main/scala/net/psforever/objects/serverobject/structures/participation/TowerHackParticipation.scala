// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
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
      //setup for ...
      import scala.concurrent.duration._
      val curr = System.currentTimeMillis()
      val contributionOpposingSize = contributionOpposing.size
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
      val soiPlayers = building.PlayersInSOI
      //1) experience from killing opposingFaction
      //Because the hack duration of towers is instantaneous, the prior period of five minutes is artificially selected.
      val baseExperienceFromFacilityCapture: Long = FacilityHackParticipation.calculateExperienceFromKills(
        FacilityHackParticipation.allocateKillsByPlayers(
          building.Position,
          building.Definition.SOIRadius.toFloat,
          curr - 5.minutes.toMillis,
          curr,
          opposingFaction,
          contributionVictor
        ),
        contributionOpposingSize
      )
      //2) peak population modifier
      //Towers should not be regarded as major battles.
      //As the population rises, the rewards decrease (dramatically).
      val populationModifier = FacilityHackParticipation.populationProgressModifier(
        victorPopulationByLayer,
        { pop =>
          if (pop > 80) 0f
          else if (pop > 39) (80 - pop).toFloat * 0.01f
          else if (pop > 25) 0.5f
          else if (pop > 19) 0.55f
          else if (pop > 9) 0.6f
          else if (pop > 5) 0.75f
          else 1f
        },
        2
      )
      //3) competition multiplier
      val competitionMultiplier: Float = FacilityHackParticipation.populationBalanceModifier(
        victorPopulationByLayer,
        opposingPopulationByLayer,
        healthyPercentage = 1.25f
      )
      //4a. individual contribution factors - by time
      //Once again, an arbitrary five minute period.
      val contributionPerPlayerByTime = playerContribution.collect {
        case (a, (_, d, t)) if d >= 300000 && math.abs(completionTime - t) < 5000 =>
          (a, 0.45f)
        case (a, (_, d, _)) if d >= 300000 =>
          (a, 0.25f)
        case (a, (_, d, t)) if math.abs(completionTime - t) < 5000 =>
          (a, 0.25f * (0.5f + (d.toFloat / 300000f)))
        case (a, (_, _, _)) =>
          (a, 0.15f)
      }
      //4b. individual contribution factors - by distance to goal (secondary_capture)
      //Because the hack duration of towers is instantaneous, distance from terminal is a more important factor
      val contributionPerPlayerByDistanceFromGoal = {
        var minDistance: Float = Float.PositiveInfinity
        val location = building
          .CaptureTerminal
          .map { terminal => terminal.Position }
          .getOrElse { hacker.Position }
        soiPlayers
          .map { p =>
            val distance = Vector3.Distance(p.Position, location)
            minDistance = math.min(minDistance, distance)
            (p.CharId, distance)
          }
          .map { case (id, distance) =>
            (id, math.max(0.15f, minDistance / distance))
          }
      }.toMap[Long, Float]
      //5) token competition bonus
      //This value will probably suck, and that's fine.
      val competitionBonus: Long = FacilityHackParticipation.competitionBonus(
        contributionVictorSize,
        contributionOpposingSize,
        steamrollPercentage = 1.25f,
        steamrollBonus = 2L,
        overwhelmingOddsPercentage = 0.5f,
        overwhelmingOddsBonus = 30L
      )
      //6. calculate overall command experience points
      val finalCep: Long = math.ceil(
        baseExperienceFromFacilityCapture *
          populationModifier *
          competitionMultiplier *
          Config.app.game.experience.cep.rate + competitionBonus
      ).toLong
      //7. reward participants
      //Classically, only players in the SOI are rewarded
      val events = building.Zone.AvatarEvents
      soiPlayers
        .filter { player =>
          player.Faction == victorFaction && player.CharId != hacker.CharId
        }
        .foreach { player =>
          val charId = player.CharId
          val contributionTimeMultiplier = contributionPerPlayerByTime.getOrElse(charId, 0.5f)
          val contributionDistanceMultiplier = contributionPerPlayerByDistanceFromGoal.getOrElse(charId, 0.5f)
          val outputValue = (finalCep * contributionTimeMultiplier * contributionDistanceMultiplier).toLong
          events ! AvatarServiceMessage(
            player.Name,
            AvatarAction.AwardCep(0, outputValue)
          )
        }
      events ! AvatarServiceMessage(hacker.Name, AvatarAction.AwardCep(hacker.CharId, finalCep))
    }

    playerContribution.clear()
    playerPopulationOverTime.reverse match {
      case entry :: _ => playerPopulationOverTime = Seq(entry)
    }
  }
}
