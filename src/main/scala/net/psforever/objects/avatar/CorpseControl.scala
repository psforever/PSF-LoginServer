// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.packet.game.{ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.{PlanetSideEmpire, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

class CorpseControl(player: Player) extends Actor with ContainableBehavior {
  def ContainerObject = player

  //private [this] val log = org.log4s.getLogger(player.Name)

  def receive: Receive = containerBehavior.orElse { case _ => ; }

  def MessageDeferredCallback(msg: Any): Unit = {
    msg match {
      case Containable.MoveItem(_, item, _) =>
        //momentarily put item back where it was originally
        val obj = ContainerObject
        obj.Find(item) match {
          case Some(slot) =>
            obj.Zone.AvatarEvents ! AvatarServiceMessage(
              player.Zone.id,
              AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectAttachMessage(obj.GUID, item.GUID, slot))
            )
          case None => ;
        }
      case _ => ;
    }
  }

  def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    val obj    = ContainerObject
    val zone   = obj.Zone
    val events = zone.AvatarEvents
    item.Faction = PlanetSideEmpire.NEUTRAL
    events ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID))
  }

  def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    val obj        = ContainerObject
    val zone       = obj.Zone
    val events     = zone.AvatarEvents
    val definition = item.Definition
    events ! AvatarServiceMessage(
      zone.id,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectCreateDetailedMessage(
          definition.ObjectId,
          item.GUID,
          ObjectCreateMessageParent(obj.GUID, slot),
          definition.Packet.DetailedConstructorData(item).get
        )
      )
    )
  }

  def SwapItemCallback(item: Equipment, fromSlot: Int): Unit = {
    val obj  = ContainerObject
    val zone = obj.Zone
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectDetachMessage(obj.GUID, item.GUID, Vector3.Zero, 0f)
      )
    )
  }
}
