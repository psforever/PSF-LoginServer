// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.Player
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalResponse, LocalServiceMessage, LocalServiceResponse}
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * An `Actor` that handles messages being dispatched to a specific `Door`.
  * @param door the `Door` object being governed
  */
class DoorControl(door: Door)
  extends PoweredAmenityControl
  with FactionAffinityBehavior.Check {
  def FactionObject: FactionAffinity = door

  val commonBehavior: Receive = checkBehavior

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case CommonMessages.Use(player, _) =>
          val zone = door.Zone
          val doorGUID = door.GUID
          if (
                player.Faction == door.Faction || (zone.GUID(zone.map.doorToLock.getOrElse(doorGUID.guid, 0)) match {
                  case Some(lock: IFFLock) =>
                    val owner            = lock.Owner.asInstanceOf[Building]
                    val playerIsOnInside = Vector3.ScalarProjection(lock.Outwards, player.Position - door.Position) < 0f
                    /*
                    If an IFF lock exists and
                    the IFF lock faction doesn't match the current player and
                    one of the following conditions are met:
                    1. player is on the inside of the door (determined by the lock orientation)
                    2. lock is hacked
                    3. facility capture terminal has been hacked
                    4. base is neutral
                    ... open the door.
                     */
                    playerIsOnInside || lock.HackedBy.isDefined || owner.CaptureTerminalIsHacked || lock.Faction == PlanetSideEmpire.NEUTRAL
                  case _ => true // no linked IFF lock, just try open the door
                })
              ) {
            openDoor(player)
          }

        case _ => ;
      }

  def unpoweredStateLogic: Receive = {
    commonBehavior
      .orElse {
        case CommonMessages.Use(player, _) =>
          //without power, the door opens freely
          openDoor(player)

        case _ => ;
      }
  }

  def openDoor(player: Player): Unit = {
    val zone = door.Zone
    val doorGUID = door.GUID
    if (!door.isOpen) {
      //global open
      door.Open = player
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.DoorOpens(Service.defaultPlayerGUID, zone, door)
      )
    }
    else {
      //the door should already open, but the requesting player does not see it as open
      sender() ! LocalServiceResponse(
        player.Name,
        Service.defaultPlayerGUID,
        LocalResponse.DoorOpens(doorGUID)
      )
    }
  }

  override def powerTurnOffCallback() : Unit = { }

  override def powerTurnOnCallback() : Unit = { }
}
