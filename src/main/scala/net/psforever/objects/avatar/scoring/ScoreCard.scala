// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.types.{PlanetSideEmpire, StatisticalCategory}

import scala.annotation.tailrec
import scala.collection.mutable

class ScoreCard() {
  private var curr: Life = Life()
  private var lives: Seq[Life] = Seq()
  private val killStatistics: mutable.HashMap[Int, Statistic] = mutable.HashMap[Int, Statistic]()
  private val assistStatistics: mutable.HashMap[Int, Statistic] = mutable.HashMap[Int, Statistic]()

  def CurrentLife: Life = curr

  def Lives: Seq[Life] = lives

  def Kills: Seq[Kill] = lives.flatMap { _.kills } ++ curr.kills

  def KillStatistics: Map[Int, Statistic] = killStatistics.toMap

  def AssistStatistics: Map[Int, Statistic] = assistStatistics.toMap

  def rate(msg: Any): Unit = {
    msg match {
      case e: EquipmentStat =>
        curr = ScoreCard.updateEquipmentStat(curr, e)
      case k: Kill =>
        curr = curr.copy(kills = k +: curr.kills)
        curr = ScoreCard.updateEquipmentStat(curr, EquipmentStat(k.info.interaction.cause.attribution, 0, 0, 1, 0))
        ScoreCard.updateStatisticsFor(killStatistics, k.info.interaction.cause.attribution, k.victim.Faction)
      case a: Assist =>
        curr = curr.copy(assists = a +: curr.assists)
        val faction = a.victim.Faction
        a.weapons.foreach { wid =>
          ScoreCard.updateStatisticsFor(assistStatistics, wid.equipment, faction)
        }
      case d: Death =>
        val expired = curr
        curr = Life()
        lives = expired.copy(death = Some(d)) +: lives
      case value: Long =>
        curr = curr.copy(supportExperience = curr.supportExperience + value)
      case _ => ()
    }
  }
}

object ScoreCard {
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
  private def updateStatisticsFor(
                                   statisticMap: mutable.HashMap[Int, Statistic],
                                   objectId: Int,
                                   victimFaction: PlanetSideEmpire.Value
                                 ): Statistic = {
    statisticMap.get(objectId) match {
      case Some(fields) =>
        val outEntry = victimFaction match {
          case PlanetSideEmpire.TR =>      fields.copy(tr_b = fields.tr_b + 1)
          case PlanetSideEmpire.NC =>      fields.copy(nc_b = fields.nc_b + 1)
          case PlanetSideEmpire.VS =>      fields.copy(vs_b = fields.vs_b + 1)
          case PlanetSideEmpire.NEUTRAL => fields.copy(ps_b = fields.ps_b + 1)
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
