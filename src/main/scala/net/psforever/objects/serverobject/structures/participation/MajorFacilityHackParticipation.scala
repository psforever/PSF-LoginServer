// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.zones.{HotSpotInfo, ZoneHotSpotProjector}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import net.psforever.util.Config

import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class MajorFacilityHackParticipation(building: Building) extends FacilityHackParticipation {
  private implicit val timeout: Timeout = 10.seconds

  private var hotSpotLayersOverTime: Seq[List[HotSpotInfo]] = Seq[List[HotSpotInfo]]()

  def TryUpdate(): Unit = {
    val list = building.PlayersInSOI
    updatePlayers(list)
    val now = System.currentTimeMillis()
    if (now - lastInfoRequest > 60000L) {
      updatePopulationOverTime(list, now, before = 900000L)
      updateHotSpotInfoOverTime()
      updateTime(now)
    }
    lastInfoRequest = now
  }

  private def updateHotSpotInfoOnly(): Future[ZoneHotSpotProjector.ExposedHeat] = {
    ask(
      building.Zone.Activity,
      ZoneHotSpotProjector.ExposeHeatForRegion(building.Position, building.Definition.SOIRadius.toFloat)
    ).mapTo[ZoneHotSpotProjector.ExposedHeat]
  }

  private def updateHotSpotInfoOverTime(): Future[ZoneHotSpotProjector.ExposedHeat] = {
    import net.psforever.objects.zones.ZoneHotSpotProjector

    import scala.concurrent.Promise
    import scala.util.Success
    val requestLayers: Promise[ZoneHotSpotProjector.ExposedHeat] = Promise[ZoneHotSpotProjector.ExposedHeat]()
    val request = updateHotSpotInfoOnly()
    requestLayers.completeWith(request)
    request.onComplete {
      case Success(ZoneHotSpotProjector.ExposedHeat(_, _, activity)) =>
        hotSpotLayersOverTime = timeSensitiveFilterAndAppend(hotSpotLayersOverTime, activity, System.currentTimeMillis(), before = 900000L)
      case _ =>
        requestLayers.completeWith(Future(ZoneHotSpotProjector.ExposedHeat(Vector3.Zero, 0, Nil)))
    }
    requestLayers.future
  }

  private def updateTime(now: Long): Unit = {
    infoRequestOverTime = timeSensitiveFilterAndAppend(infoRequestOverTime, now, now, before = 900000L)
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
    val (victorFaction, opposingFaction, flagCarrier) = if (!isResecured) {
      val carrier = building.GetFlagSocket.flatMap(_.previousFlag).flatMap(_.Carrier)
      (attackingFaction, defenderFaction, carrier)
    } else {
      (defenderFaction, attackingFaction, None)
    }
    val (contributionVictor, contributionOpposing, _) = {
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
        building.Definition.SOIRadius - 50f,
        hackStart,
        completionTime,
        opposingFaction,
        contributionVictor,
        contributionOpposingSize
      )
      //setup for ...
      val populationIndices = playerPopulationOverTime.indices
      val allFactions = PlanetSideEmpire.values.filterNot { _ == PlanetSideEmpire.NEUTRAL }.toSeq
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
          if (pop > 59) 0.5f
          else if (pop > 29) 0.4f
          else if (pop > 25) 0.3f
          else 0.25f
        },
        3
      )
      //3) competition bonus
      val competitionBonus: Long = FacilityHackParticipation.competitionBonus(
        contributionVictorSize,
        contributionOpposingSize,
        steamrollPercentage = 1.25f,
        steamrollBonus = 5L,
        overwhelmingOddsPercentage = 0.5f,
        overwhelmingOddsBonus = 100L
      )
      //4) competition multiplier
      val competitionMultiplier: Float = {
        val populationBalanceModifier: Float = FacilityHackParticipation.populationBalanceModifier(
          victorPopulationByLayer,
          opposingPopulationByLayer,
          healthyPercentage = 1.25f
        )
        //compensate for heat
        val regionHeatMapProgression = {
          /*
          transform the different layers of the facility heat map timeline into a progressing timeline of regional hotspot information;
          where the grouping are of simultaneous hotspots,
          the letter indicates a unique hotspot,
          and the number an identifier between related hotspots:
          ((A-1, B-2, C-3), (D-1, E-2, F-3), (G-1, H-2, I-3)) ... (1->(A, D, G), 2->(B, E, H), 3->(C, F, I))
           */
          val finalMap = mutable.HashMap[Vector3, Map[PlanetSideEmpire.Value, Seq[Long]]]()
            .addAll(hotSpotLayersOverTime.head.map { entry => (entry.DisplayLocation, Map.empty) })
          //note: this pre-seeding of keys allows us to skip a getOrElse call in the foldLeft
          hotSpotLayersOverTime.foldLeft(finalMap) { (map, list) =>
            list.foreach { entry =>
              val key = entry.DisplayLocation
              val newValues = entry.Activity.map { case (f, e) => (f, e.Heat.toLong) }
              val combinedValues = map(key).map { case (f, e) => (f, e :+ newValues(f)) }
              map.put(key, combinedValues)
            }
            map
          }.toMap
          finalMap //explicit for no good reason
        }
        val heatVictorMap = FacilityHackParticipation.diffHeatForFactionMap(regionHeatMapProgression, victorFaction).values
        val heatAgainstMap = FacilityHackParticipation.diffHeatForFactionMap(regionHeatMapProgression, opposingFaction).values
        val heatMapModifier = FacilityHackParticipation.heatMapComparison(heatVictorMap, heatAgainstMap)
        heatMapModifier * populationBalanceModifier
      }
      //5) hack time modifier
      val overallTimeMultiplier: Float = if (isResecured) {
        math.max(0.5f + (hackTime - completionTime) / hackTime, 1f)
      } else {
        1f
      }
      //calculate overall command experience points and the individual player multiplier
      val finalCep: Long = math.ceil(
        math.max(1L, baseExperienceFromFacilityCapture + competitionBonus) *
          populationModifier *
          competitionMultiplier *
          overallTimeMultiplier *
          Config.app.game.cepRate
      ).toLong
      val contributionPerPlayerByTime = playerContribution.collect {
        case (a, (_, d, t)) if d >= 600000 && math.abs(completionTime - t) < 5000 =>
          (a, 1f)
        case (a, (_, d, t)) if math.abs(completionTime - t) < 5000 =>
          (a, d.toFloat / 6000000f)
        case (a, (_, d, t)) =>
          (a, math.max(0, d - completionTime + t).toFloat / 6000000f)
      }
      //reward participant(s)
      // classically, only players in the SOI are rewarded
      val events = building.Zone.AvatarEvents
      building.PlayersInSOI
        .filter { player =>
          player.Faction == victorFaction && player.CharId != hacker.CharId && !flagCarrier.contains(player)
        }
        .foreach { player =>
          val contributionMultiplier = contributionPerPlayerByTime.getOrElse(player.CharId, 1f)
          events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(0, (finalCep * contributionMultiplier).toLong))
        }
      events ! AvatarServiceMessage(hacker.Name, AvatarAction.AwardCep(hacker.CharId, finalCep))
      flagCarrier.collect {
        player => events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(player.CharId, (finalCep * 0.5f).toLong))
      }
    }
  }
}
