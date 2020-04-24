// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.{BoomerTrigger, PlanetSideGameObject, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.packet.game.{ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.Vector3
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

trait Containable {
  _ : Actor =>
  //val log = org.log4s.getLogger("Container.behavior")
  var movingItem : Boolean = false

  def ContainerObject : PlanetSideServerObject with Container

  def behavior : Receive = {
    case Containable.RemoveItemFromSlot(None, Some(slot)) =>
      val source = ContainerObject
      val(_, item) = Containable.TryRemoveItemFromSlot(source, slot)
      sender ! Containable.ItemFromSlot(source, item, Some(slot))

    case Containable.RemoveItemFromSlot(Some(item), _) =>
      val source = ContainerObject
      val(slot, _) = Containable.TryRemoveItemFromSlot(source, item)
      sender ! Containable.ItemFromSlot(source, Some(item), slot)

    case Containable.PutItemInSlot(item, dest) =>
      val destination = ContainerObject
      Containable.PutItemInSlot(destination, item, dest) match {
        case (true, swapItem) =>
          movingItem = false
          sender ! Containable.ItemPutInSlot(destination, item, dest, swapItem)
        case (false, _) =>
          movingItem = false
          sender ! Containable.CanNotItemPutInSlot(destination, item, dest)
      }

    case Containable.PutItemAway(item) =>
      val destination = ContainerObject
      Containable.PutItemAway(destination, item) match {
        case Some(dest) =>
          movingItem = false
          sender ! Containable.ItemPutInSlot(destination, item, dest, None)
        case _ =>
          movingItem = false
          sender ! Containable.CanNotItemPutInSlot(destination, item, -1)
      }

    case Containable.PutItemInSlotOrAway(item, dest) =>
      val destination = ContainerObject
      Containable.PutItemInSlotOrAway(destination, item, dest) match {
        case (Some(slot), swapItem) =>
          movingItem = false
          sender ! Containable.ItemPutInSlot(destination, item, slot, swapItem)
        case (None, _) =>
          movingItem = false
          sender ! Containable.CanNotItemPutInSlot(destination, item, dest.getOrElse(-1))
      }

    case msg : Containable.MoveItem if movingItem =>
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(100 milliseconds, self, msg)

    case Containable.MoveItem(source, equipment, destSlot) =>
      val destination = ContainerObject
      val item = equipment
      val dest = destSlot
      val sourceIsDestination = source == destination
      implicit val timeout = Timeout(1000 milliseconds)
      movingItem = true
      val result = if(sourceIsDestination) {
        val(slot, _) = Containable.TryRemoveItemFromSlot(source, item)
        Future { Containable.ItemFromSlot(source, Some(item), slot) }
      }
      else {
        ask(source.Actor, Containable.RemoveItemFromSlot(item))
      }
      result.onSuccess {
        case Containable.ItemFromSlot(_, Some(thing), Some(slot)) if thing == item =>
          Containable.PutItemInSlot(destination, item, dest) match {
            case (true, Some(swapItem)) =>
              if(sourceIsDestination) {
                Containable.PutItemInSlot(source, swapItem, slot)
                movingItem = false
              }
              else {
                source.Actor forward Containable.PutItemInSlot(swapItem, slot)
              }
            case (true, None) =>
              movingItem = false
            case _ => ;
          }
        case _ => ;
      }
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
        source.Zone.AvatarEvents ! AvatarServiceMessage(source.Zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID))
        (slot, Some(item))
      case None =>
        (None, None)
    }
  }

  def TryRemoveItemFromSlot(source : PlanetSideServerObject with Container, slot : Int) : (Option[Int], Option[Equipment]) = {
    val item = source.Slot(slot).Equipment
    source.Slot(slot).Equipment = None
    item match {
      case Some(thing) =>
        source.Zone.AvatarEvents ! AvatarServiceMessage(source.Zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, thing.GUID))
        (Some(slot), item)
      case None =>
        (None, None)
    }
  }

  def PutItemInSlot(destination : PlanetSideServerObject with Container, item : Equipment, dest : Int) : (Boolean, Option[Equipment]) = {
    if(Containable.PermitEquipmentStow(item, destination)) {
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
        swapItem match {
          case Some(swap) =>
            destination.Zone.AvatarEvents ! AvatarServiceMessage(destination.Zone.Id, AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectDetachMessage(destination.GUID, swap.GUID, Vector3.Zero, 0f)))
          case None =>
        }
        destination.Slot(swapSlot).Equipment = None
        if((destination.Slot(dest).Equipment = item).contains(item)) {
          destination.Zone.AvatarEvents ! AvatarServiceMessage(
            destination.Zone.Id,
            AvatarAction.SendResponse(
              Service.defaultPlayerGUID,
              ObjectCreateDetailedMessage(
                item.Definition.ObjectId,
                item.GUID,
                ObjectCreateMessageParent(destination.GUID, dest),
                item.Definition.Packet.DetailedConstructorData(item).get
              )
            )
          )
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

  def PutItemAway(destination : PlanetSideServerObject with Container, item : Equipment) : Option[Int] = {
    destination.Fit(item) match {
      case out @ Some(dest) =>
        destination.Slot(dest).Equipment = item
        out
      case _ =>
        None
    }
  }

  def FitItemIntSlot(destination : PlanetSideServerObject with Container, item : Equipment) : Option[Int] = {
    destination.Fit(item) match {
      case Some(slot) =>
        PutItemInSlot(destination, item, slot) match {
          case (true, _) => Some(slot)
          case (false, _) => None
        }
      case None =>
        None
    }
  }

  def PutItemInSlotOrAway(destination : PlanetSideServerObject with Container, item : Equipment, dest : Option[Int]) : (Option[Int], Option[Equipment]) = {
    dest match {
      case Some(destSlot) =>
        Containable.PutItemInSlot(destination, item, destSlot) match {
          case (true, swapItem) =>
            (dest, swapItem)
          case (false, _) =>
            Containable.FitItemIntSlot(destination, item) match {
              case out @ Some(_) =>
                (out, None)
              case None =>
                (None, None)
            }
        }
      case None =>
        Containable.FitItemIntSlot(destination, item) match {
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
    * @param obj na
    * @return `true`, if the object is allowed to contain the type of equipment object
    */
  def PermitEquipmentStow(equipment : Equipment, obj : PlanetSideGameObject with Container) : Boolean = {
    equipment match {
      case _ : BoomerTrigger =>
        obj.isInstanceOf[Player] //a BoomerTrigger can only be stowed in a player's holsters or inventory
      case _ =>
        true
    }
  }
}
