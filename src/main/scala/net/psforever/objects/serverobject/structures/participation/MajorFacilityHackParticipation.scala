// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.structures.participation

import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.sourcing.{PlayerSource, UniquePlayer}
import net.psforever.objects.zones.{HotSpotInfo, ZoneHotSpotProjector}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGeneratorState, Vector3}
import net.psforever.util.Config
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.avatar.scoring.Kill
import net.psforever.objects.zones.exp.ToDatabase

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
//    val request = updateHotSpotInfoOnly()
//    requestLayers.completeWith(request)
//    request.onComplete {
//      case Success(ZoneHotSpotProjector.ExposedHeat(_, _, activity)) =>
//        hotSpotLayersOverTime = timeSensitiveFilterAndAppend(hotSpotLayersOverTime, activity, System.currentTimeMillis() - 900000L)
//      case _ =>
//        requestLayers.completeWith(Future(ZoneHotSpotProjector.ExposedHeat(building.Position.xy, building.Definition.SOIRadius, Nil)))
//    }
    requestLayers.completeWith(Future(ZoneHotSpotProjector.ExposedHeat(building.Position.xy, building.Definition.SOIRadius.toFloat, Nil)))
    requestLayers.future
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
    val socketOpt = building.GetFlagSocket
    val (victorFaction, opposingFaction, hasFlag, flagCarrier) = if (!isResecured) {
      val carrier = socketOpt.flatMap(_.previousFlag).flatMap(_.Carrier)
      (attackingFaction, defenderFaction, socketOpt.nonEmpty, carrier)
    } else {
      (defenderFaction, attackingFaction, socketOpt.nonEmpty, None)
    }
    val (contributionVictor, contributionOpposing, _) = {
      val (a, b1) = playerContribution.partition { case (_, (p, _, _)) => p.Faction == victorFaction }
      val (b, c) = b1.partition { case (_, (p, _, _)) => p.Faction == opposingFaction }
      (a.values, b.values, c.values)
    }
    val contributionVictorSize = contributionVictor.size
    if (contributionVictorSize > 0) {
      //setup for ...
      val populationIndices = playerPopulationOverTime.indices
      val allFactions = PlanetSideEmpire.values.filterNot { _ == PlanetSideEmpire.NEUTRAL }.toSeq
      val (victorPopulationByLayer, opposingPopulationByLayer) = {
        val individualPopulationByLayer = allFactions.map { f =>
          (f, populationIndices.indices.map { i => playerPopulationOverTime(i)(f) })
        }.toMap[PlanetSideEmpire.Value, Seq[Int]]
        (individualPopulationByLayer(victorFaction), individualPopulationByLayer(opposingFaction))
      }
      val contributionOpposingSize = contributionOpposing.size
      val killsByPlayersNotInTower = eliminateClosestTowerFromParticipating(
        building,
        FacilityHackParticipation.allocateKillsByPlayers(
          building.Position,
          building.Definition.SOIRadius.toFloat,
          hackStart,
          completionTime,
          opposingFaction,
          contributionOpposing
        )
      )
      //1) experience from killing opposingFaction across duration of hack
      //The kills that occurred in the facility's attached field tower's sphere of influence have been eliminated from consideration.
      val baseExperienceFromFacilityCapture: Long = FacilityHackParticipation.calculateExperienceFromKills(
        killsByPlayersNotInTower,
        contributionOpposingSize
      )
      //2) peak population modifier
      //Large facility battles should be well-rewarded.
      val populationModifier = FacilityHackParticipation.populationProgressModifier(
        opposingPopulationByLayer,
        { pop =>
          if (pop > 75) 0.9f
          else if (pop > 59) 0.6f
          else if (pop > 29) 0.55f
          else if (pop > 25) 0.5f
          else 0.45f
        },
        4
      )
      //3) competition multiplier
      val competitionMultiplier: Float = {
        val populationBalanceModifier: Float = FacilityHackParticipation.populationBalanceModifier(
          victorPopulationByLayer,
          opposingPopulationByLayer,
          healthyPercentage = 1.5f
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
            .addAll(
              hotSpotLayersOverTime.flatMap { entry =>
                entry.map { f => (f.DisplayLocation, Map.empty[PlanetSideEmpire.Value, Seq[Long]]) }
              }
            )
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
        val heatMapModifier = FacilityHackParticipation.heatMapComparison(
          FacilityHackParticipation.diffHeatForFactionMap(regionHeatMapProgression, victorFaction).values,
          FacilityHackParticipation.diffHeatForFactionMap(regionHeatMapProgression, opposingFaction).values
        )
        heatMapModifier * populationBalanceModifier
      }
      //4) hack time modifier
      //Captured major facilities without a lattice link unit and resecured major facilities with a lattice link unit
      // incur the full hack time if the module is not transported to a friendly facility
      //Captured major facilities with a lattice link unit and resecure major facilities without a lattice link uit
      // will incur an abbreviated duration
      val overallTimeMultiplier: Float = {
        if (
          building.Faction == PlanetSideEmpire.NEUTRAL ||
            building.NtuLevel == 0 ||
            building.Generator.map { _.Condition }.contains(PlanetSideGeneratorState.Destroyed)
        ) { //the facility ran out of nanites or power during the hack or became neutral
          0f
        } else if (hasFlag) {
          if (completionTime >= hackTime) { //hack timed out without llu delivery
            0.25f
          } else if (isResecured) {
            0.5f + (if (hackTime <= completionTime * 0.3f) {
              completionTime.toFloat / hackTime.toFloat
            } else if (hackTime >= completionTime * 0.6f) {
              (hackTime - completionTime).toFloat / hackTime.toFloat
            } else {
              0f
            })
          } else {
            0.5f + (hackTime - completionTime).toFloat / (2f * hackTime)
          }
        } else {
          if (isResecured) {
            0.5f + (hackTime - completionTime).toFloat / (2f * hackTime)
          } else {
            0.5f
          }
        }
      }
      //5. individual contribution factors - by time
      val contributionPerPlayerByTime = playerContribution.collect {
        case (a, (_, d, t)) if d >= 600000 && math.abs(completionTime - t) < 5000 =>
          (a, 0.45f)
        case (a, (_, d, _)) if d >= 600000 =>
          (a, 0.25f)
        case (a, (_, d, t)) if math.abs(completionTime - t) < 5000 =>
          (a, 0.25f * (0.5f + (d.toFloat / 600000f)))
        case (a, (_, _, _)) =>
          (a, 0.15f)
      }
      //6. competition bonus
      //This value will probably suck, and that's fine.
      val competitionBonus: Long = FacilityHackParticipation.competitionBonus(
        contributionVictorSize,
        contributionOpposingSize,
        steamrollPercentage = 1.25f,
        steamrollBonus = 5L,
        overwhelmingOddsPercentage = 0.5f,
        overwhelmingOddsBonus = 15L
      )
      //7. calculate overall command experience points
      val finalCep: Long = math.ceil(
        math.max(0L, baseExperienceFromFacilityCapture) *
          populationModifier *
          competitionMultiplier *
          overallTimeMultiplier *
          Config.app.game.experience.cep.rate + competitionBonus
      ).toLong
      //8. reward participants
      //Classically, only players in the SOI are rewarded, and the llu runner too
      val hackerId = hacker.CharId
      val events = building.Zone.AvatarEvents
      val playersInSoi = building.PlayersInSOI
      if (playersInSoi.exists(_.CharId == hackerId) && flagCarrier.map(_.CharId).getOrElse(0L) != hackerId) {
        events ! AvatarServiceMessage(hacker.Name, AvatarAction.AwardCep(hackerId, finalCep))
      }
      playersInSoi
        .filter { player => player.Faction == victorFaction && player.CharId != hackerId }
        .foreach { player =>
          val charId = player.CharId
          val contributionMultiplier = contributionPerPlayerByTime.getOrElse(charId, 1f)
          val outputValue = (finalCep * contributionMultiplier).toLong
          events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(0, outputValue))
        }
      flagCarrier.collect {
        case player if !isResecured =>
          val charId: Long = player.CharId
          val finalModifiedCep: Long = {
            val durationPoints: Long = (hackTime - completionTime) / 1500L
            val betterDurationPoints: Long = if (durationPoints >= 200L) {
              durationPoints
            } else {
              200L + durationPoints
            }
            math.min(
              betterDurationPoints,
              (finalCep * Config.app.game.experience.cep.lluCarrierModifier).toLong
            )
          }
          ToDatabase.reportFacilityCapture(
            charId,
            building.Zone.Number,
            building.GUID.guid,
            finalModifiedCep,
            expType="llu"
          )
          events ! AvatarServiceMessage(player.Name, AvatarAction.AwardCep(charId, finalModifiedCep))
      }
    }
  }

  private def eliminateClosestTowerFromParticipating(
                                                      building: Building,
                                                      list: Iterable[(UniquePlayer, Float, Seq[Kill])]
                                                    ): Iterable[(UniquePlayer, Float, Seq[Kill])] = {
    val buildingPosition = building.Position.xy
    building
      .Zone
      .Buildings
      .values
      .filter { building => building.BuildingType == StructureType.Tower }
      .minByOption { tower => Vector3.DistanceSquared(buildingPosition, tower.Position.xy) }
      .map { tower =>
        val towerPosition = tower.Position.xy
        val towerRadius = math.pow(tower.Definition.SOIRadius.toDouble * 0.7d, 2d).toFloat
        list
          .map { case (p, f, kills) =>
            val filteredKills = kills.filter { kill => Vector3.DistanceSquared(kill.victim.Position.xy, towerPosition) <= towerRadius }
            (p, f, filteredKills)
          }
          .filter { case (_, _, kills) => kills.nonEmpty }
      }
      .getOrElse(list)
  }
}
