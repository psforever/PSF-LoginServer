// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.blockmap.SectorPopulation

trait InteractsWithZone
  extends PlanetSideServerObject {
  /** interactions for this particular entity is allowed */
  private var _allowInteraction: Boolean = true
  /** maximum interaction range used to generate the commonly tested sector */
  private var interactionRange: Float = 0.1f

  /**
    * If the interactive permissions of this entity change.
    */
  def allowInteraction: Boolean = _allowInteraction

  /**
    * If the interactive permissions of this entity change,
    * trigger a formal change to the interaction methodology.
    * @param permit whether or not interaction is permitted
    * @return whether or not interaction is permitted
    */
  def allowInteraction_=(permit: Boolean): Boolean = {
    val before = _allowInteraction
    _allowInteraction = permit
    if (before != permit) {
      if (permit) {
        doInteractions()
      } else {
        interactions.foreach ( _.resetInteraction(target = this) )
      }
    }
    _allowInteraction
  }

  private var interactions: List[ZoneInteraction] = List()

  def interaction(func: ZoneInteraction): List[ZoneInteraction] = {
    interactions = interactions :+ func
    if (func.range > interactionRange) {
      interactionRange = func.range
    }
    interactions
  }

  def interaction(): List[ZoneInteraction] = interactions

  def getInteractionSector(): SectorPopulation = {
    this.Zone.blockMap.sector(this.Position, interactionRange)
  }

  def doInteractions(): Unit = {
    val sector = getInteractionSector()
    interactions.foreach { _.interaction(sector, target = this) }
  }

  def zoneInteractions(): Unit = {
    if (_allowInteraction) {
      doInteractions()
    }
  }

  def resetInteractions(): Unit = {
    interactions.foreach { _.resetInteraction(target = this) }
  }
}

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
