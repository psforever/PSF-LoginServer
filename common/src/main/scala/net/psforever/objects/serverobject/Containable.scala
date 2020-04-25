// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.packet.game.{ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.Vector3
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}


trait Containable {
  _ : Actor =>
  private var waitWhileMovingItems : Boolean = false

  def ContainerObject : PlanetSideServerObject with Container

  final val containerBehavior : Receive = {
    case Containable.RemoveItemFromSlot(None, Some(slot)) =>
      sender ! LocalRemoveItemFromSlot(slot)

    case Containable.RemoveItemFromSlot(Some(item), _) =>
      sender ! LocalRemoveItemFromSlot(item)

    case Containable.PutItemInSlot(item, dest) =>
      sender ! LocalPutItemInSlot(item, dest)

    case Containable.PutItemAway(item) =>
      sender ! LocalPutItemAway(item)

    case Containable.PutItemInSlotOrAway(item, dest) =>
      sender ! LocalPutItemInSlotOrAway(item, dest)

    case msg : Containable.MoveItem if waitWhileMovingItems =>
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(100 milliseconds, self, msg)

    case Containable.MoveItem(source, equipment, destSlot) =>
      val to = sender
      val destination = ContainerObject
      val item = equipment
      val dest = destSlot
      val sourceIsDestination = source == destination
      implicit val timeout = Timeout(1000 milliseconds)
      waitWhileMovingItems = true
      val result = if(sourceIsDestination) {
        Future { LocalRemoveItemFromSlot(item) }
      }
      else {
        ask(source.Actor, Containable.RemoveItemFromSlot(item))
      }
      result.onSuccess {
        case Containable.ItemFromSlot(_, Some(thing), Some(slot)) if thing == item =>
          LocalPutItemInSlot(item, dest) match {
            case Containable.ItemPutInSlot(_, _, _, Some(swapItem)) => //passing condition
              if(sourceIsDestination) {
                sender ! LocalPutItemInSlotOrAway(swapItem, Some(slot))
              }
              else {
                to.tell(Containable.PutItemInSlotOrAway(swapItem, Some(slot)), source.Actor)
              }
              waitWhileMovingItems = false
            case Containable.ItemPutInSlot(_, _, _, None) => //passing condition
              waitWhileMovingItems = false
            case _ =>
              waitWhileMovingItems = false
          }
        case _ => ;
      }
      result.onFailure {
        case _ =>
          waitWhileMovingItems = false
      }
  }

  final def LocalRemoveItemFromSlot(slot : Int) : Any = {
    val source = ContainerObject
    val (_, item) = Containable.TryRemoveItemFromSlot(source, slot)
    item match {
      case Some(thing) => RemoveItemFromSlotCallback(thing)
      case None => ;
    }
    Containable.ItemFromSlot(source, item, Some(slot))
  }

  final def LocalRemoveItemFromSlot(item : Equipment) : Any = {
    val source = ContainerObject
    val(slot, retItem) = Containable.TryRemoveItemFromSlot(source, item)
    retItem match {
      case Some(thing) => RemoveItemFromSlotCallback(thing)
      case None => ;
    }
    Containable.ItemFromSlot(source, Some(item), slot)
  }

  final def LocalPutItemInSlot(item : Equipment, dest : Int) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemInSlot(destination, item, dest) match {
      case (true, swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing)
          case None => ;
        }
        PutItemInSlotCallback(item, dest)
        waitWhileMovingItems = false
        Containable.ItemPutInSlot(destination, item, dest, swapItem)
      case (false, _) =>
        waitWhileMovingItems = false
        Containable.CanNotItemPutInSlot(destination, item, dest)
    }
  }

  final def LocalPutItemAway(item : Equipment) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemAway(destination, item) match {
      case Some(dest) =>
        PutItemInSlotCallback(item, dest)
        waitWhileMovingItems = false
        Containable.ItemPutInSlot(destination, item, dest, None)
      case _ =>
        waitWhileMovingItems = false
        Containable.CanNotItemPutInSlot(destination, item, -1)
    }
  }

  final def LocalPutItemInSlotOrAway(item : Equipment, dest : Option[Int]) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemInSlotOrAway(destination, item, dest) match {
      case (Some(slot), swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing)
          case None => ;
        }
        PutItemInSlotCallback(item, slot)
        waitWhileMovingItems = false
        Containable.ItemPutInSlot(destination, item, slot, swapItem)
      case (None, _) =>
        waitWhileMovingItems = false
        Containable.CanNotItemPutInSlot(destination, item, dest.getOrElse(-1))
    }
  }

  def RemoveItemFromSlotCallback(item : Equipment) : Unit = {
    val zone = ContainerObject.Zone
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID))
  }

  def PutItemInSlotCallback(item : Equipment, slot : Int) : Unit = {
    val obj = ContainerObject
    val zone = obj.Zone
    val definition = item.Definition
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.Id,
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

  def SwapItemCallback(item : Equipment) : Unit = {
    val obj = ContainerObject
    val zone = obj.Zone
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectDetachMessage(obj.GUID, item.GUID, Vector3.Zero, 0f)))
  }
}

object Containable {
  final case class RemoveItemFromSlot(item : Option[Equipment], slot : Option[Int])

  object RemoveItemFromSlot {
    def apply(slot : Int) : RemoveItemFromSlot = RemoveItemFromSlot(None, Some(slot))

    def apply(item : Equipment) : RemoveItemFromSlot = RemoveItemFromSlot(Some(item), None)
  }

  final case class ItemFromSlot(obj : PlanetSideServerObject with Container, item : Option[Equipment], slot : Option[Int])

  final case class PutItemInSlot(item : Equipment, dest : Int)

  final case class PutItemAway(item : Equipment)

  final case class PutItemInSlotOrAway(item : Equipment, dest : Option[Int])

  final case class ItemPutInSlot(obj : PlanetSideServerObject with Container, item : Equipment, dest : Int, swapped_item : Option[Equipment])

  final case class CanNotItemPutInSlot(obj : PlanetSideServerObject with Container, item : Equipment, dest : Int)

  final case class MoveItem(source : PlanetSideServerObject with Container, item : Equipment, dest : Int)

  //-//

  def TryRemoveItemFromSlot(source : PlanetSideServerObject with Container, item : Equipment) : (Option[Int], Option[Equipment]) = {
    source.Find(item) match {
      case slot @ Some(index) =>
        source.Slot(index).Equipment = None
        (slot, Some(item))
      case None =>
        (None, None)
    }
  }

  def TryRemoveItemFromSlot(source : PlanetSideServerObject with Container, slot : Int) : (Option[Int], Option[Equipment]) = {
    val item = source.Slot(slot).Equipment
    source.Slot(slot).Equipment = None
    item match {
      case Some(_) =>
        (Some(slot), item)
      case None =>
        (None, None)
    }
  }

  def TryPutItemInSlot(destination : PlanetSideServerObject with Container, item : Equipment, dest : Int) : (Boolean, Option[Equipment]) = {
    if(Containable.PermitEquipmentStow(destination, item)) {
      val tile = item.Definition.Tile
      val destinationCollisionTest = destination.Collisions(dest, tile.Width, tile.Height)
      if(
        destinationCollisionTest match {
          case Success(Nil) | Success(List(_)) => true //no item or one item to swap
          case _ => false //abort when too many items at destination or other failure case
        }
      ) {
        //insert and swap, if applicable
        val destSlot = destination.Slot(dest)
        val (swapItem, swapSlot) = destinationCollisionTest match {
          case Success(List(InventoryItem(obj, start))) =>
            (Some(obj), start)
          case _ =>
            (None, dest)
        }
        destination.Slot(swapSlot).Equipment = None
        if((destination.Slot(dest).Equipment = item).contains(item)) {
          (true, swapItem)
        }
        else {
          //put the swapItem back
          destSlot.Equipment = swapItem
          (false, None)
        }
      }
      else {
        //can not insert at destination
        (false, None)
      }
    }
    else {
      //blocked insertion (object type not permitted in container)
      (false, None)
    }
  }

  def TryPutItemAway(destination : PlanetSideServerObject with Container, item : Equipment) : Option[Int] = {
    destination.Fit(item) match {
      case out @ Some(dest) =>
        destination.Slot(dest).Equipment = item
        out
      case _ =>
        None
    }
  }

  def TryFitItemIntSlot(destination : PlanetSideServerObject with Container, item : Equipment) : Option[Int] = {
    destination.Fit(item) match {
      case Some(slot) =>
        TryPutItemInSlot(destination, item, slot) match {
          case (true, _) => Some(slot)
          case (false, _) => None
        }
      case None =>
        None
    }
  }

  def TryPutItemInSlotOrAway(destination : PlanetSideServerObject with Container, item : Equipment, dest : Option[Int]) : (Option[Int], Option[Equipment]) = {
    dest match {
      case Some(destSlot) =>
        Containable.TryPutItemInSlot(destination, item, destSlot) match {
          case (true, swapItem) =>
            (dest, swapItem)
          case (false, _) =>
            Containable.TryFitItemIntSlot(destination, item) match {
              case out @ Some(_) =>
                (out, None)
              case None =>
                (None, None)
            }
        }
      case None =>
        Containable.TryFitItemIntSlot(destination, item) match {
          case out @ Some(_) =>
            (out, None)
          case None =>
            (None, None)
        }
    }
  }

  /**
    * na
    * @param equipment na
    * @return `true`, if the object is allowed to contain the type of equipment object
    */
  def PermitEquipmentStow(obj : PlanetSideServerObject with Container, equipment : Equipment) : Boolean = {
    import net.psforever.objects.{BoomerTrigger, Player}
    equipment match {
      case _ : BoomerTrigger =>
        obj.isInstanceOf[Player] //a BoomerTrigger can only be stowed in a player's holsters or inventory
      case _ =>
        true
    }
  }
}
