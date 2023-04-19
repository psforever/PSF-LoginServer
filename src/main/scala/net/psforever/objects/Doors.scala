// Copyright (c) 2023 PSForever
package net.psforever.objects

import net.psforever.objects.serverobject.doors.Door
import net.psforever.types.Vector3

object Doors {
  /**
   * If a player was considered as holding the door open,
   * check that if that player is still in the same zone as a door
   * and is still standing within a certain distance of that door.
   * If the target player no longer qualifies to hold the door open,
   * search for a new player to hold the door open.
   * If the target player did not initially exist (the door was closed),
   * do not search any further.
   * @param door door that is installed somewhere
   * @param maximumDistance permissible square distance between the player and the door
   * @return optional player;
   *         `None` if the no player can trigger the door
   */
  def testForTargetHoldingDoorOpen(
                                    door: Door,
                                    maximumDistance: Float
                                  ): Option[Player] = {
    door.Open
      .flatMap {
        testForSpecificTargetHoldingDoorOpen(_, door, maximumDistance * maximumDistance)
          .orElse { testForAnyTargetHoldingDoorOpen(door, maximumDistance) }
      }
  }

  /**
   * Check that a player is in the same zone as a door
   * and is standing within a certain distance of that door.
   * @see `Vector3.MagnitudeSquared`
   * @param player player who is standing somewhere
   * @param door door that is installed somewhere
   * @param maximumDistance permissible square distance between the player and the door
   *                        before one can not influence the other
   * @return optional player (same as parameter);
   *         `None` if the parameter player can not trigger the door
   */
  def testForSpecificTargetHoldingDoorOpen(
                                            player: Player,
                                            door: Door,
                                            maximumDistance: Float
                                          ): Option[Player] = {
    if (player.Zone == door.Zone && Vector3.MagnitudeSquared(door.Position - player.Position) <= maximumDistance) {
      Some(player)
    } else {
      None
    }
  }

  /**
   * Find a player, any player, that can hold the door open.
   * Prop it open with a dead body if no one is available.
   * @param door door that is installed somewhere
   * @param maximumDistance permissible square distance between a player and the door
   *                        before one can not influence the other
   * @return optional player;
   *         `None` if no player can trigger the door
   */
  private def testForAnyTargetHoldingDoorOpen(
                                               door: Door,
                                               maximumDistance: Float
                                             ): Option[Player] = {
    //search for nearby players and nearby former players who would block open the door
    val zone = door.Zone
    val maximumDistanceSq = maximumDistance * maximumDistance
    val bmap = zone.blockMap.sector(door.Position, maximumDistance)
    (bmap.livePlayerList ++ bmap.corpseList)
      .find { testForSpecificTargetHoldingDoorOpen(_, door, maximumDistanceSq).nonEmpty } match {
      case out @ Some(newOpener) =>
        door.Open = newOpener //another player is near the door, keep it open
        out
      case _ =>
        None
    }
  }
}
