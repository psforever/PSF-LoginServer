// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.containable

import akka.actor.{Actor, ActorRef}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, InventoryItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.Zone
import net.psforever.objects.{BoomerTrigger, GlobalDefinitions, Player}
import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/** Parent of all standard (input) messages handled by a `ContainableBehavior` object for the purposes of item transfer */
sealed trait ContainableMsg

/** `ContainableBehavior` messages that are allowed to be temporarily blocked in event of a complicated item transfer */
sealed trait DeferrableMsg extends ContainableMsg

/**
  * A mixin for handling synchronized movement of `Equipment` items into or out from `Container` entities.
  * The most important feature of this synchronization is the movemement of equipment
  * out from one container into another container
  * without causing representation overlap, overwriting, or unintended stacking of other equipment
  * including equipment that has not yet been inserted.
  */
trait ContainableBehavior {
  _: Actor =>
  def ContainerObject: PlanetSideServerObject with Container

  /**
    * A flag for handling deferred messages during an attempt at complicated item movement (`MoveItem`) procedures.
    * Complicated item movement procedures generally occur when the source and the destination are not the same container.
    * The flag is set to `1` on the destination and is designed to block interference other normal insertion messages
    * by taking those messages and pushing them back into the mailbox after a short delay.
    * If two attempts on the same destination occur due to extremely coincidental item movement messages,
    * the flag is set to `2` and most all messages involving item movement and item insertion are deferred.
    * The destination is set back to normal - flag to `0` - when both of the attempts short-circuit due to timeout.
    */
  private var waitOnMoveItemOps: Int = 0

  final val containerBehavior: Receive = {
    /* messages that modify delivery order */
    case ContainableBehavior.Wait() => Wait()

    case ContainableBehavior.Resume() => Resume()

    case repeatMsg @ ContainableBehavior.Defer(msg, sentBy) =>
      //received a previously blocked message; is it still blocked?
      msg match {
        case _: ContainableMsg if waitOnMoveItemOps == 2 => RepeatMessageLater(repeatMsg)
        case _: DeferrableMsg if waitOnMoveItemOps == 1  => RepeatMessageLater(repeatMsg)
        case _                                           => self.tell(msg, sentBy)
      }

    case msg: ContainableMsg if waitOnMoveItemOps == 2 =>
      //all standard messages are blocked
      RepeatMessageLater(ContainableBehavior.Defer(msg, sender()))
      MessageDeferredCallback(msg)

    case msg: DeferrableMsg if waitOnMoveItemOps == 1 =>
      //insertion messages not related to an item move attempt are blocked
      RepeatMessageLater(ContainableBehavior.Defer(msg, sender()))
      MessageDeferredCallback(msg)

    /* normal messages */
    case Containable.RemoveItemFromSlot(None, Some(slot)) =>
      sender() ! LocalRemoveItemFromSlot(slot)

    case Containable.RemoveItemFromSlot(Some(item), _) =>
      sender() ! LocalRemoveItemFromSlot(item)

    case Containable.PutItemInSlot(item, dest) =>
      /* can be deferred */
      sender() ! LocalPutItemInSlot(item, dest)

    case Containable.PutItemInSlotOnly(item, dest) =>
      /* can be deferred */
      sender() ! LocalPutItemInSlotOnly(item, dest)

    case Containable.PutItemAway(item) =>
      /* can be deferred */
      sender() ! LocalPutItemAway(item)

    case Containable.PutItemInSlotOrAway(item, dest) =>
      /* can be deferred */
      sender() ! LocalPutItemInSlotOrAway(item, dest)

    case msg @ Containable.MoveItem(destination, equipment, destSlot) =>
      /* can be deferred */
      if (ContainableBehavior.TestPutItemInSlot(destination, equipment, destSlot).nonEmpty) { //test early, before we try to move the item
        val source = ContainerObject
        val item   = equipment
        val dest   = destSlot
        LocalRemoveItemFromSlot(item) match {
          case Containable.ItemFromSlot(_, Some(_), slot @ Some(originalSlot)) =>
            if (source eq destination) {
              //when source and destination are the same, moving the item can be performed in one pass
              LocalPutItemInSlot(item, dest) match {
                case Containable.ItemPutInSlot(_, _, _, None) => ; //success
                case Containable.ItemPutInSlot(_, _, _, Some(swapItem)) => //success, but with swap item
                  LocalPutItemInSlotOnlyOrAway(swapItem, slot) match {
                    case Containable.ItemPutInSlot(_, _, _, None) => ;
                    case _ =>
                      source.Zone.Ground.tell(
                        Zone.Ground.DropItem(swapItem, source.Position, Vector3.z(source.Orientation.z)),
                        source.Actor
                      ) //drop it
                  }
                case _: Containable.CanNotPutItemInSlot => //failure case ; try restore original item placement
                  LocalPutItemInSlot(item, originalSlot)
              }
            } else {
              //destination sync
              destination.Actor ! ContainableBehavior.Wait()
              implicit val timeout = new Timeout(1000 milliseconds)
              val moveItemOver     = ask(destination.Actor, ContainableBehavior.MoveItemPutItemInSlot(item, dest))
              moveItemOver.onComplete {
                case Success(Containable.ItemPutInSlot(_, _, _, None)) => ; //successful

                case Success(Containable.ItemPutInSlot(_, _, _, Some(swapItem))) => //successful, but with swap item
                  PutItBackOrDropIt(source, swapItem, slot, destination.Actor)

                case Success(_: Containable.CanNotPutItemInSlot) => //failure case ; try restore original item placement
                  PutItBackOrDropIt(source, item, slot, source.Actor)

                case Failure(_) => //failure case ; try restore original item placement
                  PutItBackOrDropIt(source, item, slot, source.Actor)

                case _ => ; //TODO what?
              }
              //always do this
              moveItemOver
                .recover { case _: AskTimeoutException => destination.Actor ! ContainableBehavior.Resume() }
                .onComplete { _ => destination.Actor ! ContainableBehavior.Resume() }
            }
          case _ => ;
          //we could not find the item to be moved in the source location; trying to act on old data?
        }
      } else {
        MessageDeferredCallback(msg)
      }

    case ContainableBehavior.MoveItemPutItemInSlot(item, dest) =>
      sender() ! LocalPutItemInSlot(item, dest)

    case ContainableBehavior.MoveItemPutItemInSlotOrAway(item, dest) =>
      sender() ! LocalPutItemInSlotOrAway(item, dest)
  }

  /* Functions (message control) */

  /**
    * Defer a message until later.
    * @see `ContainableBehavior.Defer`
    * @see `DeferrableMsg`
    * @param msg the message to defer
    */
  def RepeatMessageLater(msg: Any): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.scheduleOnce(100 milliseconds, self, msg)
  }

  /**
    * Increment the flag for blocking messages.
    */
  def Wait(): Unit = {
    waitOnMoveItemOps = math.min(waitOnMoveItemOps + 1, 2)
  }

  /**
    * Decrement the flag for blocking messages.
    */
  def Resume(): Unit = {
    waitOnMoveItemOps = math.max(0, waitOnMoveItemOps - 1)
  }

  /**
    * Stop blocking messages.
    */
  def Reset(): Unit = {
    waitOnMoveItemOps = 0
  }

  /* Functions (item transfer) */

  private def LocalRemoveItemFromSlot(slot: Int): Any = {
    val source          = ContainerObject
    val (outSlot, item) = ContainableBehavior.TryRemoveItemFromSlot(source, slot)
    item match {
      case Some(thing) => RemoveItemFromSlotCallback(thing, outSlot.get)
      case None        => ;
    }
    Containable.ItemFromSlot(source, item, outSlot)
  }

  private def LocalRemoveItemFromSlot(item: Equipment): Any = {
    val source          = ContainerObject
    val (slot, retItem) = ContainableBehavior.TryRemoveItemFromSlot(source, item)
    retItem match {
      case Some(thing) => RemoveItemFromSlotCallback(thing, slot.get)
      case None        => ;
    }
    Containable.ItemFromSlot(source, Some(item), slot)
  }

  private def LocalPutItemInSlot(item: Equipment, dest: Int): Any = {
    val destination = ContainerObject
    ContainableBehavior.TryPutItemInSlot(destination, item, dest) match {
      case (true, swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing, dest)
          case None        => ;
        }
        PutItemInSlotCallback(item, dest)
        Containable.ItemPutInSlot(destination, item, dest, swapItem)
      case (false, _) =>
        Containable.CanNotPutItemInSlot(destination, item, dest)
    }
  }

  private def LocalPutItemInSlotOnly(item: Equipment, dest: Int): Any = {
    val destination = ContainerObject
    if (ContainableBehavior.TryPutItemInSlotOnly(destination, item, dest)) {
      PutItemInSlotCallback(item, dest)
      Containable.ItemPutInSlot(destination, item, dest, None)
    } else {
      Containable.CanNotPutItemInSlot(destination, item, dest)
    }
  }

  private def LocalPutItemAway(item: Equipment): Any = {
    val destination = ContainerObject
    ContainableBehavior.TryPutItemAway(destination, item) match {
      case Some(dest) =>
        PutItemInSlotCallback(item, dest)
        Containable.ItemPutInSlot(destination, item, dest, None)
      case _ =>
        Containable.CanNotPutItemInSlot(destination, item, -1)
    }
  }

  private def LocalPutItemInSlotOrAway(item: Equipment, dest: Option[Int]): Any = {
    val destination = ContainerObject
    ContainableBehavior.TryPutItemInSlotOrAway(destination, item, dest) match {
      case (Some(slot), swapItem) =>
        swapItem match {
          case Some(thing) => SwapItemCallback(thing, slot)
          case None        => ;
        }
        PutItemInSlotCallback(item, slot)
        Containable.ItemPutInSlot(destination, item, slot, swapItem)
      case (None, _) =>
        Containable.CanNotPutItemInSlot(destination, item, dest.getOrElse(-1))
    }
  }

  private def LocalPutItemInSlotOnlyOrAway(item: Equipment, dest: Option[Int]): Any = {
    val destination = ContainerObject
    ContainableBehavior.TryPutItemInSlotOnlyOrAway(destination, item, dest) match {
      case (Some(slot), None) =>
        PutItemInSlotCallback(item, slot)
        Containable.ItemPutInSlot(destination, item, slot, None)
      case _ =>
        Containable.CanNotPutItemInSlot(destination, item, dest.getOrElse(-1))
    }
  }

  /**
    * A controlled response where, in certain situations,
    * it is appropriate to attempt to place an item into a specific container,
    * first testing a specific slot,
    * and attempting anywhere available in the container if not that slot,
    * and, if nowhere is available, then it gets dropped on the ground.
    * The inserted item is not permitted to swap places with another item in this case.
    * @param container the container
    * @param item the item to be inserted
    * @param slot in which slot the insertion is prioritized (upper left corner of item)
    * @param to a recipient to redirect the response message
    * @param timeout how long the request has to complete before expiring
    */
  private def PutItBackOrDropIt(
      container: PlanetSideServerObject with Container,
      item: Equipment,
      slot: Option[Int],
      to: ActorRef
  )(implicit timeout: Timeout): Unit = {
    val restore = ask(container.Actor, ContainableBehavior.MoveItemPutItemInSlotOrAway(item, slot))
    restore.onComplete {
      case Success(_: Containable.CanNotPutItemInSlot) =>
        container.Zone.Ground
          .tell(Zone.Ground.DropItem(item, container.Position, Vector3.z(container.Orientation.z)), to)

      case Failure(_) =>
        container.Zone.Ground
          .tell(Zone.Ground.DropItem(item, container.Position, Vector3.z(container.Orientation.z)), to)

      case _ => ; //normal success; //TODO what?
    }
  }

  /**
    * Reaction to the initial deferrence of a message that should handle the visual aspects of not immediately addressing the message.
    * To be implemented.
    * @param msg the deferred message
    */
  def MessageDeferredCallback(msg: Any): Unit

  /**
    * Reaction to an item being removed a container.
    * To be implemented.
    * @param item the item that was removed
    * @param slot the slot from which is was removed
    */
  def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit

  /**
    * Reaction to an item being placed into a container.
    * To be implemented.
    * @param item the item that was removed
    * @param slot the slot from which is was removed
    */
  def PutItemInSlotCallback(item: Equipment, slot: Int): Unit

  /**
    * Reaction to the existence of a swap item being produced from a container into the environment.
    * To be implemented.
    * @param item the item that was removed
    * @param fromSlot the slot from where the item was removed (where it previous was)
    */
  def SwapItemCallback(item: Equipment, fromSlot: Int): Unit
}

object ContainableBehavior {

  /** Control message for temporarily blocking some messages to maintain integrity of underlying `Container` object */
  private case class Wait()

  /** Control message for unblocking all messages */
  private case class Resume()

  /** Internal message for the purpose of refreshing a blocked message in the mailbox */
  private case class Defer(msg: Any, from: ActorRef)

  /* The same as `PutItemInSlot`, but is not a `DeferrableMsg` for the purposes of completing a `MoveItem` */
  private case class MoveItemPutItemInSlot(item: Equipment, slot: Int) extends ContainableMsg
  /* The same as `PutItemInSlotOrAway`, but is not a `DeferrableMsg` for the purposes of completing a `MoveItem` */
  private case class MoveItemPutItemInSlotOrAway(item: Equipment, slot: Option[Int]) extends ContainableMsg

  /* Functions */

  /**
    * If the target item can be found in a container, remove the item from the container.
    * This process can fail if the item can not be found or if it can not be removed for some reason.
    * @see `Container.Find`
    * @see `EquipmentSlot.Equipment`
    * @param source the container in which the `item` is currently located
    * @param item the item to be removed
    * @return a `Tuple` of two optional values;
    *         the first is from what index in the container the `item` was removed, if it was removed;
    *         the second is the item again, if it has been removed;
    *         will use `(None, None)` to report failure
    */
  def TryRemoveItemFromSlot(
      source: PlanetSideServerObject with Container,
      item: Equipment
  ): (Option[Int], Option[Equipment]) = {
    source.Find(item) match {
      case slot @ Some(index) =>
        source.Slot(index).Equipment = None
        if (source.Slot(index).Equipment.isEmpty) {
          (slot, Some(item))
        } else {
          (None, None)
        }
      case None =>
        (None, None)
    }
  }

  /**
    * If the target slot of a container contains an item, remove that item from the container
    * fromthe upper left corner position of the item as found in the container.
    * This process can fail if no item can be found or if it can not be removed for some reason.
    * @see `Container.Find`
    * @see `EquipmentSlot.Equipment`
    * @param source the container in which the `slot` is to be searched
    * @param slot where the container will be searched
    * @return a `Tuple` of two values;
    *         the first is from what `slot` in the container an `item` was removed, if any item removed;
    *         the second is the item, if it has been removed;
    *         will use `(None, None)` to report failure
    */
  def TryRemoveItemFromSlot(
      source: PlanetSideServerObject with Container,
      slot: Int
  ): (Option[Int], Option[Equipment]) = {
    val (item, outSlot) = source.Slot(slot).Equipment match {
      case Some(thing) => (Some(thing), source.Find(thing))
      case None        => (None, None)
    }
    source.Slot(slot).Equipment = None
    item match {
      case Some(_) if item.nonEmpty && source.Slot(slot).Equipment.isEmpty =>
        (outSlot, item)
      case _ =>
        (None, None)
    }
  }

  /**
    * Are the conditions for an item insertion acceptable?
    * If another item occupies the expected region of insertion (collision of bounding regions),
    * the insertion can still be permitted with the assumption that
    * the displaced item ("swap item") will have to be put somewhere else.
    * @see `ContainableBehavior.PermitEquipmentStow`
    * @see `Container.Collisions`
    * @see `InventoryTile`
    * @param destination the container
    * @param item the item to be tested for insertion
    * @param dest the upper left corner of the insertion position
    * @return the results of the insertion test, if an insertion can be permitted;
    *         `None`, otherwise, and the insertion is not permitted
    */
  def TestPutItemInSlot(
      destination: PlanetSideServerObject with Container,
      item: Equipment,
      dest: Int
  ): Option[List[InventoryItem]] = {
    if (ContainableBehavior.PermitEquipmentStow(destination, item)) {
      val tile                     = item.Definition.Tile
      val destinationCollisionTest = destination.Collisions(dest, tile.Width, tile.Height)
      destinationCollisionTest match {
        case Success(Nil)           => Some(Nil) //no item to swap
        case Success(out @ List(_)) => Some(out) //one item to swap
        case _                      => None      //abort when too many items at destination or other failure case
      }
    } else {
      None //blocked insertion (object type not permitted in container)
    }
  }

  /**
    * Put an item in a container at the given position.
    * The inserted item may swap places with another item.
    * If the new item can not be inserted, the swap item is kept in its original position.
    * @param destination the container
    * @param item the item to be inserted
    * @param dest in which slot the insertion is expected to occur (upper left corner of item)
    * @return a `Tuple` of two values;
    *         the first is `true` if the insertion occurred; and, `false`, otherwise
    *         the second is an optional item that was removed from a coincidental position in the container ("swap item")
    */
  def TryPutItemInSlot(
      destination: PlanetSideServerObject with Container,
      item: Equipment,
      dest: Int
  ): (Boolean, Option[Equipment]) = {
    ContainableBehavior.TestPutItemInSlot(destination, item, dest) match {
      case Some(results) =>
        //insert and swap, if applicable
        val (swapItem, swapSlot) = results match {
          case List(InventoryItem(obj, start)) => (Some(obj), start)
          case _                               => (None, dest)
        }
        destination.Slot(swapSlot).Equipment = None
        if ((destination.Slot(dest).Equipment = item).contains(item)) {
          (true, swapItem)
        } else {
          //put the swapItem back
          destination.Slot(swapSlot).Equipment = swapItem
          (false, None)
        }
      case None =>
        (false, None)
    }
  }

  /**
    * Put an item in a container at the given position.
    * The inserted item is not permitted to swap places with another item in this case.
    * @param destination the container
    * @param item the item to be inserted
    * @param dest in which slot the insertion is expected to occur (upper left corner of item)
    * @return `true` if the insertion occurred;
    *        `false`, otherwise
    */
  def TryPutItemInSlotOnly(destination: PlanetSideServerObject with Container, item: Equipment, dest: Int): Boolean = {
    ContainableBehavior.TestPutItemInSlot(destination, item, dest).contains(Nil) && (destination.Slot(dest).Equipment =
      item).contains(item)
  }

  /**
    * Put an item in a container in the whatever position it cleanly fits.
    * The inserted item will not swap places with another item in this case.
    * @param destination the container
    * @param item the item to be inserted
    * @return the slot index of the insertion point;
    *         `None`, if a clean insertion is not possible
    */
  def TryPutItemAway(destination: PlanetSideServerObject with Container, item: Equipment): Option[Int] = {
    destination.Fit(item) match {
      case out @ Some(dest)
          if ContainableBehavior.PermitEquipmentStow(destination, item) && (destination.Slot(dest).Equipment = item)
            .contains(item) =>
        out
      case _ =>
        None
    }
  }

  /**
    * Attempt to put an item in a container at the given position.
    * The inserted item may swap places with another item at this time.
    * If the targeted insertion at this position fails,
    * attempt to put the item in the container in the whatever position it cleanly fits.
    * @param destination the container
    * @param item the item to be inserted
    * @param dest in which specific slot the insertion is first tested (upper left corner of item)
    * @return na
    */
  def TryPutItemInSlotOrAway(
      destination: PlanetSideServerObject with Container,
      item: Equipment,
      dest: Option[Int]
  ): (Option[Int], Option[Equipment]) = {
    (dest match {
      case Some(slot) => ContainableBehavior.TryPutItemInSlot(destination, item, slot)
      case None       => (false, None)
    }) match {
      case (true, swapItem) =>
        (dest, swapItem)
      case _ =>
        ContainableBehavior.TryPutItemAway(destination, item) match {
          case out @ Some(_) => (out, None)
          case None          => (None, None)
        }
    }
  }

  /**
    * Attempt to put an item in a container at the given position.
    * The inserted item may not swap places with another item at this time.
    * If the targeted insertion at this position fails,
    * attempt to put the item in the container in the whatever position it cleanly fits.
    * @param destination the container
    * @param item the item to be inserted
    * @param dest in which specific slot the insertion is first tested (upper left corner of item)
    * @return na
    */
  def TryPutItemInSlotOnlyOrAway(
      destination: PlanetSideServerObject with Container,
      item: Equipment,
      dest: Option[Int]
  ): (Option[Int], Option[Equipment]) = {
    (dest match {
      case Some(slot) if ContainableBehavior.TestPutItemInSlot(destination, item, slot).contains(Nil) =>
        ContainableBehavior.TryPutItemInSlot(destination, item, slot)
      case _ => (false, None)
    }) match {
      case (true, swapItem) =>
        (dest, swapItem)
      case _ =>
        ContainableBehavior.TryPutItemAway(destination, item) match {
          case out @ Some(_) => (out, None)
          case None          => (None, None)
        }
    }
  }

  /**
    * Apply incontestable, arbitrary limitations
    * whereby certain items are denied insertion into certain containers
    * for vaguely documented but assuredly fantastic excuses on the part of the developer.
    * @param destination the container
    * @param equipment the item to be inserted
    * @return `true`, if the object is allowed to contain the type of equipment object;
    *        `false`, otherwise
    */
  def PermitEquipmentStow(destination: PlanetSideServerObject with Container, equipment: Equipment): Boolean = {
    import net.psforever.objects.{BoomerTrigger, Player}
    equipment match {
      case _: BoomerTrigger =>
        //a BoomerTrigger can only be stowed in a player's holsters or inventory
        //this is only a requirement until they, and their Boomer explosive complement, are cleaned-up properly
        destination.isInstanceOf[Player]
      case _ =>
        true
    }
  }

  /**
    * A predicate used to determine if an `InventoryItem` object contains `Equipment` that should be dropped.
    * Used to filter through lists of object data before it is placed into a player's inventory.
    * Drop the item if:<br>
    * - the item is cavern equipment<br>
    * - the item is a `BoomerTrigger` type object<br>
    * - the item is a `router_telepad` type object<br>
    * - the item is another faction's exclusive equipment
    * @param tplayer the player
    * @return true if the item is to be dropped; false, otherwise
    */
  def DropPredicate(tplayer: Player): InventoryItem => Boolean =
    entry => {
      val objDef  = entry.obj.Definition
      val faction = GlobalDefinitions.isFactionEquipment(objDef)
      GlobalDefinitions.isCavernEquipment(objDef) ||
      objDef == GlobalDefinitions.router_telepad ||
      entry.obj.isInstanceOf[BoomerTrigger] ||
      (faction != tplayer.Faction && faction != PlanetSideEmpire.NEUTRAL)
    }
}

object Containable {
  final case class RemoveItemFromSlot(item: Option[Equipment], slot: Option[Int]) extends ContainableMsg

  object RemoveItemFromSlot {
    def apply(slot: Int): RemoveItemFromSlot = RemoveItemFromSlot(None, Some(slot))

    def apply(item: Equipment): RemoveItemFromSlot = RemoveItemFromSlot(Some(item), None)
  }

  /**
    * A response for the `RemoveItemFromSlot` message.
    * It serves the dual purpose of reporting a missing item (by not reporting any slot information)
    * and reporting no item at a given position (by not reporting any item information).
    * @param obj the container
    * @param item the equipment that was removed
    * @param slot the index position from which any item was removed
    */
  final case class ItemFromSlot(obj: PlanetSideServerObject with Container, item: Option[Equipment], slot: Option[Int])

  final case class PutItemInSlot(item: Equipment, slot: Int) extends DeferrableMsg

  final case class PutItemInSlotOnly(item: Equipment, slot: Int) extends DeferrableMsg

  final case class PutItemAway(item: Equipment) extends DeferrableMsg

  final case class PutItemInSlotOrAway(item: Equipment, slot: Option[Int]) extends DeferrableMsg

  /**
    * A "successful insertion" response for the variety message of messages that attempt to insert an item into a container.
    * @param obj the container
    * @param item the equipment that was inserted
    * @param slot the slot position into which the item was inserted
    * @param swapped_item any other item, previously in the container, that was displaced to make room for this insertion
    */
  final case class ItemPutInSlot(
      obj: PlanetSideServerObject with Container,
      item: Equipment,
      slot: Int,
      swapped_item: Option[Equipment]
  )

  /**
    * A "failed insertion" response for the variety message of messages that attempt to insert an item into a container.
    * @param obj the container
    * @param item the equipment that was not inserted
    * @param slot the slot position into which the item should have been inserted;
    *             `-1` if no insertion slot was reported in the original message or discovered in the process of inserting
    */
  final case class CanNotPutItemInSlot(obj: PlanetSideServerObject with Container, item: Equipment, slot: Int)

  /**
    * The item should already be contained by us.
    * The item is being removed from our containment and placed into a fixed slot position in another container.
    * `MoveItem` is a process that may be complicated and is one reason why `DeferrableMsg`s are employed.
    * @param destination the container into which the item is being placed
    * @param item the item
    * @param destination_slot where in the destination container the item is being placed
    */
  final case class MoveItem(destination: PlanetSideServerObject with Container, item: Equipment, destination_slot: Int)
      extends DeferrableMsg
}
