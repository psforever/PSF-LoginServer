// Copyright (c) 2021 PSForever
package net.psforever.objects.ce

import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.{InteractsWithZone, Zone, ZoneInteraction, ZoneInteractionType}
import net.psforever.objects.{BoomerDeployable, ExplosiveDeployable}
import net.psforever.types.PlanetSideGUID

case object MineInteraction extends ZoneInteractionType

/**
  * This game entity may infrequently test whether it may interact with game world deployable extra-territorial munitions.
  * "Interact", here, is a graceful word for "trample upon" and the consequence should be an explosion
  * and maybe death.
  */
class InteractWithMines(val range: Float)
  extends ZoneInteraction {
  /**
    * mines that, though detected, are skipped from being alerted;
    * in between interaction tests, a memory of the mines that were messaged last test are retained and
    * are excluded from being messaged this test;
    * mines that are detected a second time are cleared from the list and are available to be messaged in the next test
    */
  private var skipTargets: List[PlanetSideGUID] = List()

  def Type = MineInteraction

  /**
    * Trample upon active mines in our current detection sector and alert those mines.
    * @param sector the portion of the block map being tested
    * @param target the fixed element in this test
    */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    val faction = target.Faction
    val targets = sector
      .deployableList
      .filter {
        case _: BoomerDeployable     => false //boomers are specific types of ExplosiveDeployable but do not count here
        case ex: ExplosiveDeployable => ex.Faction != faction &&
                                        Zone.distanceCheck(target, ex, ex.Definition.triggerRadius)
        case _                       => false
      }
    val notSkipped = targets.filterNot { t => skipTargets.contains(t.GUID) }
    skipTargets = notSkipped.map { _.GUID }
    notSkipped.foreach { t =>
      t.Actor ! ExplosiveDeployable.TriggeredBy(target)
    }
  }

  /**
    * Mines can not be un-exploded or un-alerted.
    * All that can be done is blanking our retained previous messaging targets.
    * @param target the fixed element in this test
    */
  def resetInteraction(target: InteractsWithZone): Unit = {
    skipTargets = List()
  }
}
