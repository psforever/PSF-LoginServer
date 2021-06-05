// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

import net.psforever.objects.serverobject.PlanetSideServerObject

trait InteractsWithZone
  extends PlanetSideServerObject {
  /** interactions for this particular entity is allowed */
  private var _allowInteraction: Boolean = true

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
      interactions.foreach { _.interaction(target = this) }
    }
    _allowInteraction
  }

  private var interactions: List[ZoneInteraction] = List()

  def interaction(func: ZoneInteraction): List[ZoneInteraction] = {
    interactions = interactions :+ func
    interactions
  }

  def interaction(): List[ZoneInteraction] = interactions

  def zoneInteractions(): Unit = {
    if (_allowInteraction) {
      interactions.foreach { _.interaction(target = this) }
    }
  }

  def resetInteractions(): Unit = {
    interactions.foreach { _.resetInteraction(target = this) }
  }
}

/**
  * The basic behavior of an entity in a zone.
  * @see `InteractsWithZone`
  * @see `Zone`
  */
trait ZoneInteraction {
  /**
    * The method by which zone interactions are tested.
    * How a target tests this interaction with elements of the target's zone.
    * @param target the fixed element in this test
    */
  def interaction(target: InteractsWithZone): Unit

  /**
    * Suspend any current interaction procedures.
    * How the interactions are undone and stability restored to elements engaged with this target,
    * even if only possible by small measure.
    * Not all interactions can be reversed.
    * @param target the fixed element in this test
    */
  def resetInteraction(target: InteractsWithZone): Unit
}
