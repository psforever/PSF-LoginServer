// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.Actor
import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.packet.game.{ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * The companion of a `Locker` that is carried with a player
  * masquerading as their sixth `EquipmentSlot` object and a sub-inventory item.
  * The `Player` class refers to it as the "fifth slot" as its permanent slot number is encoded as `0x85`.
  * The inventory of this object is accessed using a game world `Locker` object (`mb_locker`).
  */
class LockerContainer extends PlanetSideServerObject with Container {
  private var faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private val inventory                       = GridInventory(30, 20)

  def Faction: PlanetSideEmpire.Value = faction

  override def Faction_=(fact: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    faction = fact
    Faction
  }

  def Inventory: GridInventory = inventory

  def VisibleSlots: Set[Int] = Set.empty[Int]

  def Definition: EquipmentDefinition = GlobalDefinitions.locker_container
}

object LockerContainer {
  def apply(): LockerContainer = {
    new LockerContainer()
  }
}

class LockerEquipment(locker: LockerContainer) extends Equipment with Container {
  private val obj = locker

  override def GUID: PlanetSideGUID = obj.GUID

  override def GUID_=(guid: PlanetSideGUID): PlanetSideGUID = obj.GUID_=(guid)

  override def HasGUID: Boolean = obj.HasGUID

  override def Invalidate(): Unit = obj.Invalidate()

  override def Faction: PlanetSideEmpire.Value = obj.Faction

  def Inventory: GridInventory = obj.Inventory

  def VisibleSlots: Set[Int] = Set.empty[Int]

  def Definition: EquipmentDefinition = obj.Definition
}

class LockerContainerControl(locker: LockerContainer, toChannel: String) extends Actor with ContainableBehavior {
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
