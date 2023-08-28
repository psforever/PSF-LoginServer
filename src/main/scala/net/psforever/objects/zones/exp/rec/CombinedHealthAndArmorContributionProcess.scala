// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp.rec

import net.psforever.objects.sourcing.SourceUniqueness
import net.psforever.objects.vital.InGameActivity
import net.psforever.objects.zones.exp.{ContributionStats, KillContributions, WeaponStats}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

class CombinedHealthAndArmorContributionProcess(
                                                 private val faction : PlanetSideEmpire.Value,
                                                 private val contributions: Map[SourceUniqueness, List[InGameActivity]],
                                                 otherSubmissions: Seq[RecoveryExperienceContribution]
                                               ) extends RecoveryExperienceContribution {
  private val excludedTargets: mutable.ListBuffer[SourceUniqueness] = mutable.ListBuffer()
  private val process: Seq[RecoveryExperienceContributionProcess] = Seq(
    new HealthRecoveryExperienceContributionProcess(faction, contributions, excludedTargets),
    new ArmorRecoveryExperienceContributionProcess(faction, contributions, excludedTargets)
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
          val groupedWeapons = entry.weapons
            .groupBy(_.equipment)
            .map {
              case (weaponId, weaponEntries) =>
                val specificEntries = weaponEntries.filter(_.equipment == weaponId)
                val amount = specificEntries.foldLeft(0)(_ + _.amount)
                val shots = specificEntries.foldLeft(0)(_ + _.shots)
                WeaponStats(weaponId, amount, shots, specificEntries.maxBy(_.time).time, 1f)
            }
            .toSeq
          (id, ContributionStats(
            player = entry.player,
            weapons = groupedWeapons,
            amount = entry.amount + entry.amount,
            total = entry.total + entry.total,
            shots = groupedWeapons.foldLeft(0)(_ + _.shots),
            time = groupedWeapons.maxBy(_.time).time
          ))
      }
  }
}
