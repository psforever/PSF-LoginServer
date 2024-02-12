// Copyright (c) 2021 PSForever
package net.psforever.objects.ce

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.turret.auto.{AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.{InteractsWithZone, ZoneInteraction, ZoneInteractionType}
import net.psforever.objects.sourcing.{SourceEntry, SourceUniqueness}
import net.psforever.types.Vector3

case object TurretInteraction extends ZoneInteractionType

/**
 * ...
 */
class InteractWithTurrets()
  extends ZoneInteraction {
  def range: Float = InteractWithTurrets.Range

  def Type: TurretInteraction.type = TurretInteraction

  /**
   * ...
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    target match {
      case clarifiedTarget: AutomatedTurret.Target =>
        val pos = clarifiedTarget.Position
        val unique = SourceUniqueness(clarifiedTarget)
        val targets = getTurretTargets(sector, pos).filter { turret => turret.Definition.AutoFire.nonEmpty && turret.Detected(unique).isEmpty }
        targets.foreach { t => t.Actor ! AutomatedTurretBehavior.Alert(clarifiedTarget) }
      case _ => ()
    }
  }

  private def getTurretTargets(
                                sector: SectorPopulation,
                                position: Vector3
                              ): Iterable[PlanetSideServerObject with AutomatedTurret] = {
    val list: Iterable[AutomatedTurret] = sector
      .deployableList
      .collect {
        case turret: AutomatedTurret => turret
      } ++ sector
      .amenityList
      .collect {
        case turret: AutomatedTurret => turret
      }
   list.collect {
     case turret: AutomatedTurret
       if {
         val stats = turret.Definition.AutoFire
         stats.nonEmpty &&
           AutomatedTurretBehavior.shapedDistanceCheckAgainstValue(stats, turret.Position, position, range, result = -1)
       } => turret
   }
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

object InteractWithTurrets {
  private lazy val Range: Float = {
    Seq(
      GlobalDefinitions.spitfire_turret,
      GlobalDefinitions.spitfire_cloaked,
      GlobalDefinitions.spitfire_aa,
      GlobalDefinitions.manned_turret
    )
      .flatMap(_.AutoFire)
      .map(_.ranges.detection)
      .max
  }
}
