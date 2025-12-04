// Copyright (c) 2021 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.Player
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.etc.RadiationReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.interaction.{InteractsWithZone, RadiationCloudInteraction, ZoneInteractionType}

case object RadiationInteraction extends ZoneInteractionType

/**
 * This game entity may infrequently test whether it may interact with radiation cloud projectiles
 * that may be emitted in the game environment for a limited amount of time.
 * Since the target entity is a player character, it gets tested for its interaction
 */
class InteractWithRadiationClouds(
                                   val range: Float,
                                   private val user: Option[Player]
                                 ) extends RadiationCloudInteraction {
  def Type: ZoneInteractionType = RadiationInteraction

  def performInteractionWithTarget(projectiles: List[Projectile], target: InteractsWithZone): Unit = {
    if (projectiles.nonEmpty) {
      val position = target.Position
      projectiles
        .foreach { projectile =>
          target.Actor ! Vitality.Damage(
            DamageInteraction(
              SourceEntry(target),
              RadiationReason(
                ProjectileQuality.modifiers(projectile, DamageResolution.Radiation, target, target.Position, user),
                target.DamageModel,
                RadiationCloudInteraction.RadiationShieldingFrom(target)
              ),
              position
            ).calculate()
          )
        }
    }
  }
}
