// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.OffhandEquipmentSlot
import net.psforever.types.PlanetSideGUID

import scala.util.Try

/**
  * This object is capable of storing ("stowing") `Equipment` within itself.<br>
  * <br>
  * The following objects are considered item containers:
  * players (their own inventory),
  * players (their corpse's loot),
  * vehicles (their trunk), and
  * lockers (contents of the player's fifth slot).
  */
trait Container {
  /**
    * A(n imperfect) reference to a generalized pool of the contained objects.
    * Having access to all of the available positions is not required.
    * The entries in this reference should definitely include all unseen positions.
    * The `GridInventory` returned by this accessor is also an implementation of `Container`.
    * @see `VisibleSlots`
    */
  def Inventory : GridInventory

  /**
    * Given an object, attempt to locate its slot.
    * All positions, `VisibleSlot` and `Inventory`, and wherever else, should be searchable.
    * @param obj the `Equipment` object
    * @return the index of the `EquipmentSlot`, or `None`
    */
  def Find(obj : Equipment) : Option[Int] = Find(obj.GUID)

  /**
    * Given globally unique identifier, if the object using it is stowed, attempt to locate its slot.
    * All positions, `VisibleSlot` and `Inventory`, and wherever else, should be searchable.
    * @param guid the GUID of the `Equipment`
    * @return the index of the `EquipmentSlot`, or `None`
    */
  def Find(guid : PlanetSideGUID) : Option[Int] = Inventory.Find(guid)

  def Fit(obj : Equipment) : Option[Int] = Fit(obj.Definition.Tile)

  def Fit(tile : InventoryTile) : Option[Int] = Inventory.Fit(tile)

  /**
    * A(n imperfect) reference to a generalized pool of the contained objects.<br>
    * <br>
    * Having access to all of the available positions is not required.
    * Only the positions that can be actively viewed by other clients are listed.
    * @see `Inventory`
    * @return all of the affected slot indices
    */
  def VisibleSlots : Set[Int]

  /**
    * Access to all stowable positions on this object by index.<br>
    * <br>
    * All positions, `VisibleSlot` and `Inventory`, and wherever else, should be reachable.
    * Regardless of the internal storage medium, the format of return is expected to be the same structure of object
    * as the most basic storage component for `Equipment`, namely, `EquipmentSlot` objects.
    * By default, it is expected to return an `EquipmentSlot` that can not be manipulated because it is `Blocked`.
    * @see `OffhandEquipmentSlot`
    * @param slotNum an index
    * @return the searchable position identified by that index
    */
  def Slot(slotNum : Int) : EquipmentSlot = {
    if(Inventory.Offset <= slotNum && slotNum <= Inventory.LastIndex) {
      Inventory.Slot(slotNum)
    }
    else {
      OffhandEquipmentSlot.BlockedSlot
    }
  }

  /**
    * Given a region of "searchable unit positions" considered as stowable,
    * determine if any previously stowed items are contained within that region.<br>
    * <br>
    * Default usage, and recommended the continued inclusion of that use,
    * is defined in terms of `Equipment` being stowed in a `GridInventory`.
    * Where the `Equipment` object is defined by the dimensions `width` and `height`,
    * starting a search at `index` will search all positions within a grid-like range of numbers.
    * Under certain searching conditions, this range may be meaningless,
    * such as is the case when searching individual positions that are normal `EquipmentSlot` objects.
    * Regardless, the value collected indicates the potential of multiple objects being discovered and
    * maintains a reference to the object itself and the slot position where the object is located.
    * (As any object can be discovered within the range, that is important.)
    * @see `GridInventory.CheckCollisionsVar`
    * @param index the position to start searching
    * @param width the width of the searchable space
    * @param height the height of the serachable space
    * @return a list of objects that have been encountered within the searchable space
    */
  def Collisions(index : Int, width : Int, height : Int) : Try[List[InventoryItem]] =
    Inventory.CheckCollisionsVar(index, width, height)
}

//object Container {
//  type ValidContainer = PlanetSideServerObject with Container
//
//  final case class GetMoveItem(where_src : Int, other : ValidContainer, where_other : Int, other_item : Option[Equipment])
//
//  final case class GiveMoveItem(cont1 : ValidContainer, where_cont1 : Int, item : Option[Equipment], cont2 : ValidContainer, where_cont2 : Int, other_item : Option[Equipment])
//
//
//  final case class TakeMoveItem(source_index : Int, destination : ValidContainer, destination_index : Int)
//
//  final case class TakeMoveItem2(item_guid : PlanetSideGUID, destination : ValidContainer, destination_index : Int)
//
//  final case class GivingMoveItem(item : Equipment, source : ValidContainer, source_index : Int, destination : ValidContainer, destination_index : Int)
//
//  final case class PutMoveItem(item : Equipment, target_index : Int, source : ValidContainer, source_index : Int)
//
//  final case class DropMoveItem(item : Equipment, source : ValidContainer, source_index : Int)
//
//  final case class ItemMoved(item : Equipment, location : ValidContainer, location_index : Int)
//
//  final case class NoMoveItem(source : ValidContainer, source_index : Int)
//}
//
//trait ContainerBehavior {
//  this : Actor =>
//
//  def ContainableObject : Container.ValidContainer
//
//  val containerBehavior : Receive = {
//    case Container.GetMoveItem(where_src, destination, where_dest, other_item) =>
//      val slot : EquipmentSlot = ContainableObject.Slot(where_src)
//      val equipment = slot.Equipment
//      slot.Equipment = None
//      sender ! Container.GiveMoveItem(ContainableObject, where_src, equipment, destination, where_dest, other_item)
//
//    case Container.TakeMoveItem(source_index, destination, destination_index) =>
//      val slot : EquipmentSlot = ContainableObject.Slot(source_index)
//      val equipment : Option[Equipment] = slot.Equipment
//      slot.Equipment = None
//      sender ! (equipment match {
//        case Some(item) =>
//          Container.GivingMoveItem(item, ContainableObject, source_index, destination, destination_index)
//        case None =>
//          Container.NoMoveItem(ContainableObject, source_index)
//      })
//
//    case Container.TakeMoveItem2(item_guid, destination, destination_index) =>
//      ContainableObject.Find(item_guid) match {
//        case Some(source_index) =>
//          val slot : EquipmentSlot = ContainableObject.Slot(source_index)
//          val equipment : Option[Equipment] = slot.Equipment
//          slot.Equipment = None
//          sender ! (equipment match {
//            case Some(item) =>
//              Container.GivingMoveItem(item, ContainableObject, source_index, destination, destination_index)
//            case None => ;
//          })
//
//        case None =>
//          sender ! Container.NoMoveItem(ContainableObject, 65535)
//      }
//
//    case Container.PutMoveItem(item, target_index, source, source_index) =>
//      val slot : EquipmentSlot = ContainableObject.Slot(target_index)
//      val equipment : Option[Equipment] = slot.Equipment
//      if( {
//        val tile = item.Definition.Tile
//        ContainableObject.Collisions(target_index, tile.Width, tile.Height) match {
//          case Success(Nil) => //no item swap
//            true
//          case Success(_ :: Nil) => //one item to swap
//            true
//          case Success(_) | scala.util.Failure(_) =>
//            false //abort when too many items at destination or other failure case
//        }
//      }) {
//        slot.Equipment = None
//        slot.Equipment = item
//        equipment match {
//          case Some(swapItem) =>
//            sender ! Container.GivingMoveItem(swapItem, ContainableObject, target_index, source, source_index)
//          case None => ;
//        }
//        sender ! Container.ItemMoved(item, ContainableObject, target_index)
//      }
//      else {
//        sender ! Container.DropMoveItem(item, source, source_index)
//      }
//  }
//}
