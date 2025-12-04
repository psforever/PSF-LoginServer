// Copyright (c) 2021 PSForever
package net.psforever.objects.zones.interaction

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.objects.zones.blockmap.SectorPopulation

trait InteractsWithZone
  extends PlanetSideServerObject
    with Vitality {
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

  def getInteractionSector: SectorPopulation = {
    this.Zone.blockMap.sector(this.Position, interactionRange)
  }

  def doInteractions(): Unit = {
    val sector = getInteractionSector
    //println(sector.environmentList.map { _.attribute }.mkString(" "))
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

  override def Definition: ObjectDefinition with VitalityDefinition
}
