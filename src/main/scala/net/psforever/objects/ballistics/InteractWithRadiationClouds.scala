// Copyright (c) 2021 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.Player
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.etc.RadiationReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.{InteractsWithZone, Zone, ZoneInteraction, ZoneInteractionType}
import net.psforever.types.PlanetSideGUID

case object RadiationInteraction extends ZoneInteractionType

/**
  * This game entity may infrequently test whether it may interact with radiation cloud projectiles
  * that may be emitted in the game environment for a limited amount of time.
  */
class InteractWithRadiationClouds(
                                   val range: Float,
                                   private val user: Option[Player]
                                 ) extends ZoneInteraction {
  /**
    * radiation clouds that, though detected, are skipped from affecting the target;
    * in between interaction tests, a memory of the clouds that were tested last are retained and
    * are excluded from being tested this next time;
    * clouds that are detected a second time are cleared from the list and are available to be tested next time
    */
  private var skipTargets: List[PlanetSideGUID] = List()

  def Type = RadiationInteraction

  /**
    * Wander into a radiation cloud and suffer the consequences.
    * @param sector the portion of the block map being tested
    * @param target the fixed element in this test
    */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    target match {
      case t: Vitality =>
        val position = target.Position
        //collect all projectiles in sector/range
        val projectiles = sector
          .projectileList
          .filter { cloud =>
            val radius = cloud.Definition.DamageRadius
            cloud.Definition.radiation_cloud && Zone.distanceCheck(target, cloud, radius * radius)
          }
          .distinct
        val notSkipped = projectiles.filterNot { t => skipTargets.contains(t.GUID) }
        skipTargets = notSkipped.map { _.GUID }
        if (notSkipped.nonEmpty) {
          //isolate one of each type of projectile
          notSkipped
            .foldLeft(Nil: List[Projectile]) {
              (acc, next) => if (acc.exists { _.profile == next.profile }) acc else next :: acc
            }
            .foreach { projectile =>
              t.Actor ! Vitality.Damage(
                DamageInteraction(
                  SourceEntry(target),
                  RadiationReason(
                    ProjectileQuality.modifiers(projectile, DamageResolution.Radiation, t, t.Position, user),
                    t.DamageModel,
                    0f
                  ),
                  position
                ).calculate()
              )
            }
        }
      case _ => ;
    }
  }

  /**
    * Any radiation clouds blocked from being tested should be cleared.
    * All that can be done is blanking our retained previous effect targets.
    * @param target the fixed element in this test
    */
  def resetInteraction(target: InteractsWithZone): Unit = {
    skipTargets = List()
  }
}
