// Copyright (c) 2020 PSForever
package net.psforever.objects.locker

import akka.actor.Actor
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game.{ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * A control agency mainly for manipulating the equipment stowed by a player in a `LockerContainer`
  * and reporting back to a specific xchannel in the event system about these changes.
  * @param locker the governed player-facing locker component
  * @param toChannel the channel to which to publish events, typically the owning player's name
  */
class LockerContainerControl(locker: LockerContainer, toChannel: String)
  extends Actor
  with ContainableBehavior {
  def ContainerObject = locker

  def receive: Receive =
    containerBehavior
      .orElse {
        case _ => ;
      }

  def MessageDeferredCallback(msg: Any): Unit = {
    msg match {
      case Containable.MoveItem(_, item, _) =>
        //momentarily put item back where it was originally
        val obj = ContainerObject
        obj.Find(item) match {
          case Some(slot) =>
            obj.Zone.AvatarEvents ! AvatarServiceMessage(
              toChannel,
              AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectAttachMessage(obj.GUID, item.GUID, slot))
            )
          case None => ;
        }
      case _ => ;
    }
  }

  def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    val zone = locker.Zone
    zone.AvatarEvents ! AvatarServiceMessage(toChannel, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID))
  }

  def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    val zone       = locker.Zone
    val definition = item.Definition
    item.Faction = PlanetSideEmpire.NEUTRAL
    zone.AvatarEvents ! AvatarServiceMessage(
      toChannel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectCreateDetailedMessage(
          definition.ObjectId,
          item.GUID,
          ObjectCreateMessageParent(locker.GUID, slot),
          definition.Packet.DetailedConstructorData(item).get
        )
      )
    )
  }

  def SwapItemCallback(item: Equipment, fromSlot: Int): Unit = {
    val zone = locker.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      toChannel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectDetachMessage(locker.GUID, item.GUID, Vector3.Zero, 0f)
      )
    )
  }
}
