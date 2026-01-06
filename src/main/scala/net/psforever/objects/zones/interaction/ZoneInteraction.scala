// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.interaction

import net.psforever.objects.zones.blockmap.SectorPopulation

trait ZoneInteractionType

/**
 * The basic behavior of an entity in a zone.
 * @see `InteractsWithZone`
 * @see `Zone`
 */
trait ZoneInteraction {
  /**
   * A categorical descriptor for this interaction.
   */
  def Type: ZoneInteractionType

  /**
   * The anticipated (radial?) distance across which this interaction affects the zone's blockmap.
   */
  def range: Float

  /**
   * The method by which zone interactions are tested.
   * How a target tests this interaction with elements of the target's zone.
   * @param sector the portion of the block map being tested
   * @param target the fixed element in this test
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit

  /**
   * Suspend any current interaction procedures.
   * How the interactions are undone and stability restored to elements engaged with this target,
   * even if only possible by small measure.
   * Not all interactions can be reversed.
   * @param target the fixed element in this test
   */
  def resetInteraction(target: InteractsWithZone): Unit
}
