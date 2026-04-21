// Copyright (c) 2025 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.dome.{ForceDomeControl, ForceDomePhysics}
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.interaction.{InteractsWithZone, ZoneInteraction, ZoneInteractionType}

case object ForceZoneProtection extends ZoneInteractionType

/**
 * Entities under the capitol force dome that have not died in its initial activation
 * do not take further damage until removed from under the dome or until the dome is deactivated.
 */
class InteractWithForceDomeProtection
  extends ZoneInteraction {
  def Type: ZoneInteractionType = ForceZoneProtection

  def range: Float = 10f

  /** increment to n, reevaluate the dome protecting the target, reset counter to 0 */
  private var protectSkipCounter: Int = 0
  /** dome currently protecting the target */
  private var protectedBy: Option[ForceDomePhysics] = None

  /**
   * If the target is protected, do conditions allow it to remain protected?
   * If the target was vulnerable, can it be protected?
   * Five second pause between evaluations (0-3, wait; 4, test).
   * @see `ForceDomeControl.TargetUnderForceDome`
   * @param sector the portion of the block map being tested
   * @param target the fixed element in this test
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    if (protectSkipCounter < 4) {
      protectSkipCounter += 1
    } else {
      protectSkipCounter = 0
      protectedBy match {
        case Some(dome)
          if dome.Perimeter.isEmpty ||
            target.Zone != dome.Zone ||
            !ForceDomeControl.TargetUnderForceDome(dome.Perimeter)(dome, target, maxDistance = 0f) =>
          resetInteraction(target)
        case Some(_) =>
          () //no action
        case None =>
          searchForInteractionCause(sector, target)
      }
    }
  }

  /**
   * Look the through the list of amenities in this sector for capitol force domes,
   * determine which force domes are energized (activated, expanded, enveloping, etc.),
   * and find the first active dome under which the target `entity` is positioned.
   * The target `entity` is considered protected and can not be damaged until further notice.
   * @see `Damageable.MakeInvulnerable`
   * @see `ForceDomeControl.TargetUnderForceDome`
   * @param sector – the portion of the block map being tested
   * @param target – the fixed element in this test
   * @return whichever force dome entity is detected to encircle this target `entity`, if any
   */
  private def searchForInteractionCause(sector: SectorPopulation, target: InteractsWithZone): Option[ForceDomePhysics] = {
    sector
      .amenityList
      .flatMap {
        case dome: ForceDomePhysics if dome.Perimeter.nonEmpty => Some(dome)
        case _ => None
      }
      .find { dome =>
        ForceDomeControl.TargetUnderForceDome(dome.Perimeter)(dome, target, maxDistance = 0f)
      }
      .map { dome =>
        applyProtection(target, dome)
        dome
      }
  }

  def applyProtection(target: InteractsWithZone, dome: ForceDomePhysics): Unit = {
    protectedBy = Some(dome)
    target.Actor ! Damageable.MakeInvulnerable
  }

  /**
   * No longer invulnerable (if ever).
   * Set the counter to force a reevaluation of the vulnerability state next turn.
   * @see `Damageable.MakeVulnerable`
   * @param target the fixed element in this test
   */
  def resetInteraction(target: InteractsWithZone): Unit = {
    protectSkipCounter = 5
    protectedBy = None
    target.Actor ! Damageable.MakeVulnerable
  }
}
