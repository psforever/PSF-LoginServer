// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.types.{PlanetSideEmpire, StatisticalCategory, StatisticalElement}

import scala.annotation.tailrec
import scala.collection.mutable

class ScoreCard() {
  private var curr: Life = Life()
  private var lives: Seq[Life] = Seq()
  private val killStatistics: mutable.HashMap[Int, Statistic] = mutable.HashMap[Int, Statistic]()
  private val assistStatistics: mutable.HashMap[Int, Statistic] = mutable.HashMap[Int, Statistic]()

  def CurrentLife: Life = curr

  def Lives: Seq[Life] = lives

  def AllLives: Seq[Life] = curr +: lives

  def Kills: Seq[Kill] = lives.flatMap { _.kills } ++ curr.kills

  def KillStatistics: Map[Int, Statistic] = killStatistics.toMap

  def AssistStatistics: Map[Int, Statistic] = assistStatistics.toMap

  def revive(): Unit = {
    curr = Life.revive(curr)
  }

  def respawn(): Unit = {
    val death = curr
    curr = Life()
    lives = death +: lives
  }

  def initStatisticForKill(targetId: Int, victimFaction: PlanetSideEmpire.Value): Statistic = {
    ScoreCard.initStatisticsFor(killStatistics, targetId, victimFaction)
  }

  def rate(msg: Any): Seq[(Int, Statistic)] = {
    msg match {
      case e: EquipmentStat =>
        curr = ScoreCard.updateEquipmentStat(curr, e)
        Nil
      case k: Kill =>
        curr = curr.copy(kills = k +: curr.kills)
        //TODO may need to expand these to include other fields later
        curr = ScoreCard.updateEquipmentStat(curr, EquipmentStat(k.info.interaction.cause.attribution, 0, 0, 1, 0))
        val wid = StatisticalElement.relatedElement(k.victim.ExoSuit).value
        Seq((wid, ScoreCard.updateStatisticsFor(killStatistics, wid, k.victim.Faction)))
      case a: Assist =>
        curr = curr.copy(assists = a +: curr.assists)
        val faction = a.victim.Faction
        //TODO may need to expand these to include other fields later
        a.weapons.map { weq =>
          val wid = weq.equipment
          (wid, ScoreCard.updateStatisticsFor(assistStatistics, wid, faction))
        }
      case d: Death =>
        curr = curr.copy(death = Some(d))
        Nil
      case value: Long =>
        curr = curr.copy(supportExperience = curr.supportExperience + value)
        Nil
      case _ =>
        Nil
    }
  }
}

object ScoreCard {
  def reviveCount(card: ScoreCard): Int = {
    reviveCount(card.CurrentLife)
  }

  def reviveCount(life: Life): Int = {
    recursiveReviveCount(life, count = 0)
  }

  def deathCount(card: ScoreCard): Int = {
    card.AllLives.foldLeft(0)(_ + deathCount(_))
  }

  private def deathCount(life: Life): Int = {
    life.prior match {
      case None => if (life.death.nonEmpty) 1 else 0
      case Some(previousLife) => recursiveReviveCount(previousLife, count = 1)
    }
  }

  @tailrec
  private def recursiveReviveCount(life: Life, count: Int): Int = {
    life.prior match {
      case None => count + 1
      case Some(previousLife) => recursiveReviveCount(previousLife, count + 1)
    }
  }

  private def updateEquipmentStat(curr: Life, entry: EquipmentStat): Life = {
    updateEquipmentStat(curr, entry, entry.objectId, entry.kills, entry.assists)
  }

  private def updateEquipmentStat(
                                   curr: Life,
                                   entry: EquipmentStat,
                                   objectId: Int,
                                   killCount: Int,
                                   assists: Int
                                 ): Life = {
    curr.equipmentStats.indexWhere { a => a.objectId == objectId } match {
      case -1 =>
        curr.copy(equipmentStats = entry +: curr.equipmentStats)
      case index =>
        val stats = curr.equipmentStats
        val old = stats(index)
        curr.copy(
          equipmentStats = (stats.take(index) :+ old.copy(
            shotsFired = old.shotsFired + entry.shotsFired,
            shotsLanded = old.shotsLanded + entry.shotsLanded,
            kills = old.kills + killCount
          )) ++ stats.drop(index+1)
        )
    }
  }

  @tailrec
  private def initStatisticsFor(
                                 statisticMap: mutable.HashMap[Int, Statistic],
                                 objectId: Int,
                                 victimFaction: PlanetSideEmpire.Value
                               ): Statistic = {
    statisticMap.get(objectId) match {
      case Some(fields) =>
        val outEntry = victimFaction match {
          case PlanetSideEmpire.TR =>      fields.copy(tr_c = fields.tr_c + 1)
          case PlanetSideEmpire.NC =>      fields.copy(nc_c = fields.nc_c + 1)
          case PlanetSideEmpire.VS =>      fields.copy(vs_c = fields.vs_c + 1)
          case PlanetSideEmpire.NEUTRAL => fields.copy(ps_c = fields.ps_c + 1)
        }
        statisticMap.put(objectId, outEntry)
        outEntry
      case _ =>
        val out = Statistic(0, 0, 0, 0, 0, 0, 0, 0)
        statisticMap.put(objectId, out)
        initStatisticsFor(statisticMap, objectId, victimFaction)
    }
  }

  @tailrec
  private def updateStatisticsFor(
                                   statisticMap: mutable.HashMap[Int, Statistic],
                                   objectId: Int,
                                   victimFaction: PlanetSideEmpire.Value
                                 ): Statistic = {
    statisticMap.get(objectId) match {
      case Some(fields) =>
        val outEntry = victimFaction match {
          case PlanetSideEmpire.TR =>      fields.copy(tr_s = fields.tr_s + 1)
          case PlanetSideEmpire.NC =>      fields.copy(nc_s = fields.nc_s + 1)
          case PlanetSideEmpire.VS =>      fields.copy(vs_s = fields.vs_s + 1)
          case PlanetSideEmpire.NEUTRAL => fields.copy(ps_s = fields.ps_s + 1)
        }
        outEntry
      case _ =>
        val out = Statistic(0, 0, 0, 0, 0, 0, 0, 0)
        statisticMap.put(objectId, out)
        updateStatisticsFor(statisticMap, objectId, victimFaction)
    }
  }

  def weaponObjectIdMap(objectId: Int): Int = {
    objectId match {
      //aphelion
      case  81 | 82  => 80
      case  90 | 92  => 88
      case  94 | 95  => 93
      case 102 | 104 => 100
      case 107 | 109 => 105
      //colossus
      case 183 | 184 => 182
      case 187 | 189 => 185
      case 192 | 194 => 190
      case 202 | 203 => 201
      case 206 | 208 => 204
      //cycler
      case 234 | 235 | 236 => 233
      //peregrine
      case 634 | 635 => 633
      case 638 | 640 => 636
      case 646 | 648 => 644
      case 650 | 651 => 649
      case 660 | 662 => 658
      //eh
      case _ => objectId
    }
  }

  def rewardKillGetCategories(victim: SourceEntry): Seq[StatisticalCategory] = {
    victim match {
      case p: PlayerSource =>
        p.seatedIn match {
          case Some((v: VehicleSource, seat: Int)) =>
            val seatCategory = if (seat == 0) {
              StatisticalCategory.DriverKilled
            } else {
              v.Definition.controlledWeapons().get(seat) match {
                case Some(_) => StatisticalCategory.GunnerKilled
                case None    => StatisticalCategory.PassengerKilled
              }
            }
            if (GlobalDefinitions.isFlightVehicle(v.Definition)) {
              Seq(StatisticalCategory.Dogfighter, seatCategory)
            } else {
              Seq(seatCategory)
            }
          case _ =>
            Seq(StatisticalCategory.Destroyed)
        }
      case _ =>
        Seq(StatisticalCategory.Destroyed)
    }
  }
}
