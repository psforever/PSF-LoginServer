// Copyright (c) 2021 PSForever
package net.psforever.objects.ce

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.turret.{AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.{InteractsWithZone, ZoneInteraction, ZoneInteractionType}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.types.Vector3

case object TurretInteraction extends ZoneInteractionType

/**
 * ...
 */
class InteractWithTurrets(val range: Float)
  extends ZoneInteraction {
  def Type: TurretInteraction.type = TurretInteraction

  /**
   * ...
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    target match {
      case clarifiedTarget: AutomatedTurret.Target =>
        val posxy = clarifiedTarget.Position.xy
        val unique = SourceEntry(clarifiedTarget).unique
        val targets = getTurretTargets(sector, posxy).filter { turret => turret.Detected(unique).isEmpty }
        targets.foreach { t => t.Actor ! AutomatedTurretBehavior.Alert(clarifiedTarget) }
      case _ => ()
    }
  }

  private def getTurretTargets(
                                sector: SectorPopulation,
                                position: Vector3
                              ): List[PlanetSideServerObject with AutomatedTurret] = {
    (sector
      .deployableList
      .collect {
        case turret: AutomatedTurret => turret
      } ++ sector
      .amenityList
      .collect {
        case turret: AutomatedTurret => turret
      })
      .filter { turret => Vector3.DistanceSquared(turret.Position.xy, position) < 625 }
  }

  /**
   * ...
   * @param target na
   */
  def resetInteraction(target: InteractsWithZone): Unit = {
    getTurretTargets(
      target.getInteractionSector(),
      target.Position.xy
    ).foreach { turret =>
      turret.Actor ! AutomatedTurretBehavior.Reset
    }
  }
}
