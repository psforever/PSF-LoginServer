// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.mount.interaction

import net.psforever.objects.ballistics.{Projectile, ProjectileQuality}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.etc.RadiationReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.resistance.StandardResistanceProfile
import net.psforever.objects.zones.interaction.{InteractsWithZone, RadiationCloudInteraction, ZoneInteractionType}

/**
 * This game entity may infrequently test whether it may interact with radiation cloud projectiles
 * that may be emitted in the game environment for a limited amount of time.
 * Since the entity in question is mountable, its occupants get tested for their interaction.
 */
class InteractWithRadiationCloudsSeatedInEntity(
                                                 private val obj: Mountable with StandardResistanceProfile,
                                                 val range: Float
                                               ) extends RadiationCloudInteraction {
  def Type: ZoneInteractionType = RadiationInMountableInteraction

  def performInteractionWithTarget(projectiles: List[Projectile], target: InteractsWithZone): Unit = {
    val mountedTargets = obj.Seats
      .values
      .collect { case seat => seat.occupant }
      .flatten
    if (projectiles.nonEmpty && mountedTargets.nonEmpty) {
      val position = target.Position
      val shielding = RadiationCloudInteraction.RadiationShieldingFrom(target)
      mountedTargets
        .flatMap(t => projectiles.map(p => (t, p)))
        .foreach { case (t, p) =>
          t.Actor ! Vitality.Damage(
            DamageInteraction(
              SourceEntry(t),
              RadiationReason(
                ProjectileQuality.modifiers(p, DamageResolution.Radiation, t, t.Position, None),
                t.DamageModel,
                shielding
              ),
              position
            ).calculate()
          )
      }
    }
  }
}

