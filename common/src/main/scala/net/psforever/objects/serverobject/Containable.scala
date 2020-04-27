// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success

trait Containable {
  _ : Actor =>
  def ContainerObject : PlanetSideServerObject with Container

  private var waitOnMoveItemOps : Int = 0

  final val containerBehavior : Receive = {
    /* messages that modify delivery order */
    case Containable.Wait() => Wait()

    case Containable.Resume() => Resume()

    case repeatMsg @ Containable.Defer(msg, sentBy) =>
      msg match {
        case _ : Containable.ContainableMsg if waitOnMoveItemOps == 2 => RepeatMessageLater(repeatMsg)
        case _ : Containable.DeferrableMessage if waitOnMoveItemOps == 1 => RepeatMessageLater(repeatMsg)
        case _ => self.tell(msg, sentBy)
      }

    case msg : Containable.ContainableMsg if waitOnMoveItemOps == 2 =>
      RepeatMessageLater(Containable.Defer(msg, sender))
      MessageDeferredCallback(msg)

    case msg : Containable.DeferrableMessage if waitOnMoveItemOps == 1 =>
      RepeatMessageLater(Containable.Defer(msg, sender))
      MessageDeferredCallback(msg)

    /* normal messages */
    case Containable.RemoveItemFromSlot(None, Some(slot)) =>
      sender ! LocalRemoveItemFromSlot(slot)

    case Containable.RemoveItemFromSlot(Some(item), _) =>
      sender ! LocalRemoveItemFromSlot(item)

    case Containable.PutItemInSlot(item, dest) => /* can be deferred */
      sender ! LocalPutItemInSlot(item, dest)

    case Containable.PutItemAway(item) => /* can be deferred */
      sender ! LocalPutItemAway(item)

    case Containable.PutItemInSlotOrAway(item, dest) => /* can be deferred */
      sender ! LocalPutItemInSlotOrAway(item, dest)

    case msg @ Containable.MoveItem(destination, equipment, destSlot) => /* can be deferred */
      if(Containable.TestPutItemInSlot(destination, equipment, destSlot)) { //test early, before we try to move the item
        val to = sender
        val source = ContainerObject
        val item = equipment
        val dest = destSlot
        LocalRemoveItemFromSlot(item) match {
          case Containable.ItemFromSlot(_, Some(_), slot @ Some(originalSlot)) =>
            if(source == destination) {
              //when source == destination, moving the item can be performed in one pass
              LocalPutItemInSlot(item, dest) match {
                case Containable.ItemPutInSlot(_, _, _, None) => ; //success
                case Containable.ItemPutInSlot(_, _, _, Some(swapItem)) => //success, but with swap item
                  Containable.TryPutItemInSlotOrAway(source, swapItem, slot) match {
                    case (Some(swapSlot), _) =>
                      PutItemInSlotCallback(swapItem, swapSlot) //depict swapped item placement
                    case _ =>
                      source.Zone.Ground.tell(Zone.Ground.DropItem(swapItem, source.Position, Vector3.z(source.Orientation.z)), to) //drop it
                  }
                case _ : Containable.CanNotPutItemInSlot => //failure case ; try restore original item placement
                  LocalPutItemInSlot(item, originalSlot)
              }
            }
            else {
              //destination sync
              destination.Actor ! Containable.Wait()
              implicit val timeout = new Timeout(1000 milliseconds)
              val moveItemOver = ask(destination.Actor, Containable.MoveItemPutItemInSlot(item, dest))
              moveItemOver.onSuccess {
                case Containable.ItemPutInSlot(_, _, _, None) => //successful
                  destination.Actor ! Containable.Resume()

                case Containable.ItemPutInSlot(_, _, _, Some(swapItem)) => //successful, but with swap item
                  destination.Actor ! Containable.Resume()
                  PutItBackOrDropIt(source, swapItem, slot, to)

                case _ : Containable.CanNotPutItemInSlot => //failure case ; try restore original item placement
                  destination.Actor ! Containable.Resume()
                  PutItBackOrDropIt(source, item, slot, to)
              }
              moveItemOver.onFailure {
                case _ =>
                  destination.Actor ! Containable.Resume()
                  PutItBackOrDropIt(source, item, slot, to)
              }
            }
          case _ => ;
            //we could not find the item to be moved in the source location; trying to act on old data?
        }
      }
      else {
        MessageDeferredCallback(msg)
      }

      case Containable.MoveItemPutItemInSlot(item, dest) =>
        sender ! LocalPutItemInSlot(item, dest)

      case Containable.MoveItemPutItemInSlotOrAway(item, dest) =>
        sender ! LocalPutItemInSlotOrAway(item, dest)
  }

  /**/

  def RepeatMessageLater(msg : Any) : Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.scheduleOnce(100 milliseconds, self, msg)
  }

  def Wait() : Unit = {
    waitOnMoveItemOps = math.min(waitOnMoveItemOps + 1, 2)
  }

  def Resume() : Unit = {
    waitOnMoveItemOps = math.max(0, waitOnMoveItemOps - 1)
  }

  def Reset() : Unit = {
    waitOnMoveItemOps = 0
  }

  /**/

  /**/

  private def LocalRemoveItemFromSlot(slot : Int) : Any = {
    val source = ContainerObject
    val (_, item) = Containable.TryRemoveItemFromSlot(source, slot)
    item match {
      case Some(thing) => RemoveItemFromSlotCallback(thing, slot)
      case None => ;
    }
    Containable.ItemFromSlot(source, item, Some(slot))
  }

  private def LocalRemoveItemFromSlot(item : Equipment) : Any = {
    val source = ContainerObject
    val(slot, retItem) = Containable.TryRemoveItemFromSlot(source, item)
    retItem match {
      case Some(thing) => RemoveItemFromSlotCallback(thing, slot.get)
      case None => ;
    }
    Containable.ItemFromSlot(source, Some(item), slot)
  }

  private def LocalPutItemInSlot(item : Equipment, dest : Int) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemInSlot(destination, item, dest) match {
      case (true, swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing)
          case None => ;
        }
        PutItemInSlotCallback(item, dest)
        Containable.ItemPutInSlot(destination, item, dest, swapItem)
      case (false, _) =>
        Containable.CanNotPutItemInSlot(destination, item, dest)
    }
  }

  private def LocalPutItemAway(item : Equipment) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemAway(destination, item) match {
      case Some(dest) =>
        PutItemInSlotCallback(item, dest)
        Containable.ItemPutInSlot(destination, item, dest, None)
      case _ =>
        Containable.CanNotPutItemInSlot(destination, item, -1)
    }
  }

  private def LocalPutItemInSlotOrAway(item : Equipment, dest : Option[Int]) : Any = {
    val destination = ContainerObject
    Containable.TryPutItemInSlotOrAway(destination, item, dest) match {
      case (Some(slot), swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing)
          case None => ;
        }
        PutItemInSlotCallback(item, slot)
        Containable.ItemPutInSlot(destination, item, slot, swapItem)
      case (None, _) =>
        Containable.CanNotPutItemInSlot(destination, item, dest.getOrElse(-1))
    }
  }

  private def PutItBackOrDropIt(container : PlanetSideServerObject with Container, item : Equipment, slot : Option[Int], to : ActorRef)(implicit timeout : Timeout) : Unit = {
    val restore = ask(container.Actor, Containable.MoveItemPutItemInSlotOrAway(item, slot))
    restore.onSuccess {
      case _ : Containable.CanNotPutItemInSlot =>
        container.Zone.Ground.tell(Zone.Ground.DropItem(item, container.Position, Vector3.z(container.Orientation.z)), to)
      case _ =>
    }
    restore.onFailure {
      case _ =>
        container.Zone.Ground.tell(Zone.Ground.DropItem(item, container.Position, Vector3.z(container.Orientation.z)), to)
    }
  }

  def MessageDeferredCallback(msg : Any) : Unit

  def RemoveItemFromSlotCallback(item : Equipment, slot : Int) : Unit

  def PutItemInSlotCallback(item : Equipment, slot : Int) : Unit

  def SwapItemCallback(item : Equipment) : Unit
}

object Containable {
  sealed trait ContainableMsg

  sealed trait DeferrableMessage extends ContainableMsg

  private case class Wait()

  private case class Resume()

  private case class Defer(msg : Any, from : ActorRef)

  final case class RemoveItemFromSlot(item : Option[Equipment], slot : Option[Int]) extends ContainableMsg

  object RemoveItemFromSlot {
    def apply(slot : Int) : RemoveItemFromSlot = RemoveItemFromSlot(None, Some(slot))

    def apply(item : Equipment) : RemoveItemFromSlot = RemoveItemFromSlot(Some(item), None)
  }

  final case class ItemFromSlot(obj : PlanetSideServerObject with Container, item : Option[Equipment], slot : Option[Int])

  final case class PutItemInSlot(item : Equipment, dest : Int) extends DeferrableMessage

  final case class PutItemAway(item : Equipment) extends DeferrableMessage

  final case class PutItemInSlotOrAway(item : Equipment, dest : Option[Int]) extends DeferrableMessage

  final case class ItemPutInSlot(obj : PlanetSideServerObject with Container, item : Equipment, dest : Int, swapped_item : Option[Equipment])

  final case class CanNotPutItemInSlot(obj : PlanetSideServerObject with Container, item : Equipment, dest : Int)

  final case class MoveItem(source : PlanetSideServerObject with Container, item : Equipment, dest : Int) extends DeferrableMessage

  private case class MoveItemPutItemInSlot(item : Equipment, dest : Int) extends ContainableMsg

  private case class MoveItemPutItemInSlotOrAway(item : Equipment, dest : Option[Int]) extends ContainableMsg

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

  def TestPutItemInSlot(destination : PlanetSideServerObject with Container, item : Equipment, dest : Int, permittedCollision : Boolean = true) : Boolean = {
    if(Containable.PermitEquipmentStow(destination, item)) {
      val tile = item.Definition.Tile
      val destinationCollisionTest = destination.Collisions(dest, tile.Width, tile.Height)
      destinationCollisionTest match {
        case Success(Nil) => true //no item to swap
        case Success(List(_)) => permittedCollision //one item to swap, if permitted
        case _ => false //abort when too many items at destination or other failure case
      }
    }
    else {
      //blocked insertion (object type not permitted in container)
      false
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
          destination.Slot(swapSlot).Equipment = swapItem
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

  def TryFitItemInSlot(destination : PlanetSideServerObject with Container, item : Equipment) : Option[Int] = {
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
    (dest match {
      case Some(destSlot) if Containable.TestPutItemInSlot(destination, item, destSlot, permittedCollision = false) =>
        Containable.TryPutItemInSlot(destination, item, destSlot) match {
          case (true, None) =>
            dest
          case _ =>
            None
        }
      case None =>
        None
    }) match {
      case out @ Some(_) =>
        (out, Some(item))
      case None =>
        Containable.TryFitItemInSlot(destination, item) match {
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
