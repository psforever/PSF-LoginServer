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
  def Inventory: GridInventory

  /**
    * Given an object, attempt to locate its slot.
    * All positions, `VisibleSlot` and `Inventory`, and wherever else, should be searchable.
    * @param obj the `Equipment` object
    * @return the index of the `EquipmentSlot`, or `None`
    */
  def Find(obj: Equipment): Option[Int] = Find(obj.GUID)

  /**
    * Given globally unique identifier, if the object using it is stowed, attempt to locate its slot.
    * All positions, `VisibleSlot` and `Inventory`, and wherever else, should be searchable.
    * @param guid the GUID of the `Equipment`
    * @return the index of the `EquipmentSlot`, or `None`
    */
  def Find(guid: PlanetSideGUID): Option[Int] = Inventory.Find(guid)

  def Fit(obj: Equipment): Option[Int] = Fit(obj.Definition.Tile)

  def Fit(tile: InventoryTile): Option[Int] = Inventory.Fit(tile)

  /**
    * A(n imperfect) reference to a generalized pool of the contained objects.<br>
    * <br>
    * Having access to all of the available positions is not required.
    * Only the positions that can be actively viewed by other clients are listed.
    * @see `Inventory`
    * @return all of the affected slot indices
    */
  def VisibleSlots: Set[Int]

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
  def Slot(slotNum: Int): EquipmentSlot = {
    if (Inventory.Offset <= slotNum && slotNum <= Inventory.LastIndex) {
      Inventory.Slot(slotNum)
    } else {
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
  def Collisions(index: Int, width: Int, height: Int): Try[List[InventoryItem]] =
    Inventory.CheckCollisionsVar(index, width, height)
}
