// Copyright (c) 2021-2025 PSForever
package net.psforever.objects.zones.interaction

import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.blockmap.SectorPopulation

/**
 * This game entity may infrequently test whether it may interact with radiation cloud projectiles
 * that may be emitted in the game environment for a limited amount of time.
 */
trait RadiationCloudInteraction extends ZoneInteraction {
  /**
   * radiation clouds that, though detected, are skipped from affecting the target;
   * in between interaction tests, a memory of the clouds that were tested last are retained and
   * are excluded from being tested this next time;
   * clouds that are detected a second time are cleared from the list and are available to be tested next time
   */
  private var damageTypesToSkip: List[ProjectileDefinition] = List()

  /**
   * Wander into a radiation cloud and suffer the consequences.
   * @param sector the portion of the block map being tested
   * @param target the fixed element in this test
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    performInteractionWithTarget(uniqueProjectileDamageToTargetInSector(sector, target), target)
  }

  /**
   * Any radiation clouds blocked from being tested should be cleared.
   * All that can be done is blanking our retained previous effect targets.
   * @param target the fixed element in this test
   */
  def resetInteraction(target: InteractsWithZone): Unit = {
    damageTypesToSkip = List()
  }

  private def uniqueProjectileDamageToTargetInSector(sector: SectorPopulation, target: InteractsWithZone): List[Projectile] = {
    lazy val targetList = List(target)
    val projectiles = sector
      .projectileList
      .filter { cloud =>
        val definition = cloud.Definition
        val radius = definition.DamageRadius
        definition.radiation_cloud &&
          Zone.allOnSameSide(cloud, definition, targetList).nonEmpty &&
          Zone.distanceCheck(target, cloud, radius * radius)
      }
    val projectilesToUse = projectiles.filterNot(p => damageTypesToSkip.contains(p.profile)).distinctBy(_.profile)
    damageTypesToSkip = projectilesToUse.map(_.profile)
    projectilesToUse
  }

  def performInteractionWithTarget(projectiles: List[Projectile], target: InteractsWithZone): Unit
}

object RadiationCloudInteraction {
  def RadiationShieldingFrom(target: InteractsWithZone): Float = {
    target match {
      case profile: ResistanceProfile => profile.RadiationShielding
      case _ => 0f
    }
  }
}
