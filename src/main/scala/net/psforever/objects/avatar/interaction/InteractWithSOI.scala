// Copyright (c) 2025 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.{InteractsWithZone, ZoneInteraction, ZoneInteractionType}
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.types.Vector3

case object SOIInteraction extends ZoneInteractionType

/**
 * na
 */
class InteractWithSOI()
  extends ZoneInteraction {

  private var skipTargetCounter: Int = 0
  private var occupiedSOIs: List[(String, Building)] = List()

  def Type: ZoneInteractionType = SOIInteraction

  def range: Float = 0f

  /**
   * na
   * @param sector the portion of the block map being tested
   * @param target the fixed element in this test
   */
  def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    if (skipTargetCounter < 4) {
      skipTargetCounter += 1
    } else {
      skipTargetCounter = 0
      val buildings = sector.buildingList
      if (occupiedSOIs.nonEmpty || buildings.nonEmpty) {
        val targetZone = target.Zone
        val targetPosition = target.Position
        val targetAsPlayer = target.asInstanceOf[Player]
        val key = targetAsPlayer.CharId
        val (ongoingOccupancy, nowUnoccupied) = occupiedSOIs.partition { case (_, b) =>
          targetZone == b.Zone && Vector3.DistanceSquared(targetPosition, b.Position) < math.pow(b.Definition.SOIRadius, 2).toFloat
        }
        val (_, targetBuildingsOnly) = ongoingOccupancy.unzip
        val (_, newOccupancy) = buildings
          .filter { b =>
            Vector3.DistanceSquared(b.Position, targetPosition) < math.pow(b.Definition.SOIRadius, 2)
          }
          .partition { b => targetBuildingsOnly.exists(_.Name.equals(b.Name)) }
        occupiedSOIs = ongoingOccupancy ++ newOccupancy.map { building => (building.Name, building) }
        nowUnoccupied.map(_._2).foreach { _.RemovePlayersFromSOI(key) }
        newOccupancy.foreach { _.AddPlayersInSOI(key, targetAsPlayer) }
      }
    }
  }

  def resetInteraction(target: InteractsWithZone): Unit = {
    skipTargetCounter = 0
    val charId = target.asInstanceOf[Player].CharId
    occupiedSOIs.map(_._2).foreach { _.RemovePlayersFromSOI(charId) }
    occupiedSOIs = List()
  }
}
