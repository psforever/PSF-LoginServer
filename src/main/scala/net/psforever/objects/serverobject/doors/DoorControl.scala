// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import akka.actor.ActorRef
import net.psforever.objects.{Default, Doors, Player}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.structures.PoweredAmenityControl
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage, LocalServiceResponse}

/**
  * An `Actor` that handles messages being dispatched to a specific `Door`.
  * @param door the `Door` object being governed
  */
class DoorControl(door: Door)
  extends PoweredAmenityControl
  with FactionAffinityBehavior.Check {
  def FactionObject: FactionAffinity = door

  private var isLocked: Boolean = false
  private var lockingMechanism: Door.LockingMechanismLogic = DoorControl.alwaysOpen

  def commonBehavior: Receive = checkBehavior
    .orElse {
      case Door.Lock =>
        isLocked = true
        if (door.isOpen) {
          val zone = door.Zone
          door.Open = None
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorSlamsShut(door))
        }

      case Door.Unlock =>
        isLocked = false

      case Door.UpdateMechanism(logic) =>
        lockingMechanism = logic
    }

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case CommonMessages.Use(player, Some(customDistance: Float)) =>
          testToOpenDoor(player, door, customDistance, sender())

        case CommonMessages.Use(player, _) =>
          testToOpenDoor(player, door, door.Definition.initialOpeningDistance, sender())

        case IFFLock.DoorOpenResponse(target: Player) if !isLocked =>
          DoorControl.openDoor(target, door)

        case _ => ()
      }

  def unpoweredStateLogic: Receive = {
    commonBehavior
      .orElse {
        case CommonMessages.Use(player, _) if !isLocked =>
          //without power, the door opens freely
          DoorControl.openDoor(player, door)

        case _ => ()
      }
  }

  /**
   * If the player is close enough to the door,
   * the locking mechanism allows for the door to open,
   * and the door is not bolted shut (locked),
   * then tell the door that it should open.
   * @param player player who is standing somewhere
   * @param door door that is installed somewhere
   * @param maximumDistance permissible square distance between the player and the door
   * @param replyTo the player's session message reference
   */
  private def testToOpenDoor(
                              player: Player,
                              door: Door,
                              maximumDistance: Float,
                              replyTo: ActorRef
                            ): Unit = {
    if (
        Doors.testForSpecificTargetHoldingDoorOpen(player, door, maximumDistance * maximumDistance).contains(player) &&
          lockingMechanism(player, door) && !isLocked
    ) {
      DoorControl.openDoor(player, door, replyTo)
    }
  }

  override def powerTurnOffCallback() : Unit = { }

  override def powerTurnOnCallback() : Unit = { }
}

object DoorControl {
  //noinspection ScalaUnusedSymbol
  def alwaysOpen(obj: PlanetSideServerObject, door: Door): Boolean = true

  /**
   * If the door is not open, open this door, propped open by the given player.
   * If the door is considered open, ensure the door is proper visible as open to the player.
   * @param player the player
   * @param door the door
   * @param replyTo the player's session message reference
   */
  private def openDoor(player: Player, door: Door, replyTo: ActorRef = Default.Actor): Unit = {
    val zone = door.Zone
    if (!door.isOpen) {
      //global open
      door.Open = player
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.DoorOpens(zone, door)
      )
    } else {
      //the door should already open, but the requesting player does not see it as open
      replyTo ! LocalServiceResponse(
        player.Name,
        Service.defaultPlayerGUID,
        LocalAction.DoorOpens(door.Zone, door)
      )
    }
  }
}
