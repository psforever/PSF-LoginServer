// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.InGameActivity
import net.psforever.objects.zones.exp.{ContributionStats, KillContributions, Support, WeaponStats}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

class CombinedHealthAndArmorContributionProcess(
                                                 private val faction : PlanetSideEmpire.Value,
                                                 private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                 otherSubmissions: Seq[RecoveryExperienceContribution]
                                               ) extends RecoveryExperienceContribution {
  private val process: Seq[RecoveryExperienceContributionProcess] = Seq(
    new HealthRecoveryExperienceContributionProcess(faction, contributions),
    new ArmorRecoveryExperienceContributionProcess(faction, contributions)
  )

  def submit(history: List[InGameActivity]): Unit = {
    for (elem <- process ++ otherSubmissions) { elem.submit(history) }
  }

  def output(): mutable.LongMap[ContributionStats] = {
    val output = combineRecoveryContributions(
      KillContributions.cullContributorImplements(process.head.output()),
      KillContributions.cullContributorImplements(process(1).output())
    )
    clear()
    output
  }

  def clear(): Unit = {
    process.foreach ( _.clear() )
  }

  private def combineRecoveryContributions(
                                            healthAssists: mutable.LongMap[ContributionStats],
                                            armorAssists: mutable.LongMap[ContributionStats]
                                          ): mutable.LongMap[ContributionStats] = {
    healthAssists
      .map {
        case out@(id, healthEntry) =>
          armorAssists.get(id) match {
            case Some(armorEntry) =>
              //healthAssists && armorAssists
              (id, healthEntry.copy(weapons = healthEntry.weapons ++ armorEntry.weapons))
            case None =>
              //healthAssists only
              out
          }
      }
      .addAll {
        //armorAssists only
        val healthKeys = healthAssists.keys.toSeq
        armorAssists.filter { case (id, _) => !healthKeys.contains(id) }
      }
      .map {
        case (id, entry) =>
          var totalShots: Int = 0
          var totalAmount: Int = 0
          var mostRecentTime: Long = 0
          val groupedWeapons = entry.weapons
            .groupBy(_.equipment)
            .map {
              case (weaponContext, weaponEntries) =>
                val specificEntries = weaponEntries.filter(_.equipment == weaponContext)
                val amount = specificEntries.foldLeft(0)(_ + _.amount)
                totalAmount = totalAmount + amount
                val shots = specificEntries.foldLeft(0)(_ + _.shots)
                totalShots = totalShots + shots
                val time = specificEntries.maxBy(_.time).time
                mostRecentTime = math.max(mostRecentTime, time)
                Support.calculateSupportExperience(
                  event = "support-heal",
                  WeaponStats(weaponContext, amount, shots, time, 1f)
                )
            }
            .toSeq
          (id, entry.copy(
            weapons = groupedWeapons,
            amount = totalAmount,
            total = math.max(entry.total, totalAmount),
            shots = totalShots,
            time = mostRecentTime
          ))
      }
  }
}
