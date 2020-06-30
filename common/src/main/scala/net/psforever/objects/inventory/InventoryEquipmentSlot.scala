// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.OffhandEquipmentSlot
import net.psforever.objects.equipment.{Equipment, EquipmentSize}

import scala.util.{Failure, Success}

/**
  * A slot-like interface for a specific grid position in an inventory.
  * The size is typically bound to anything that can be stowed which encompasses most all `Equipment`.
  * The capacity of this `EquipmentSlot` is essentially treated as 1x1.
  * Upon insertions, however, the capacity temporarily is treated as the size of the item being inserted (unless `None`).
  * This allows a proper check for insertion collision.<br>
  * <br>
  * Rather than operating on a fixed-size slot, this "slot" represents an inventory region that either includes `slot` or starts at `slot`.
  * When determining the contents of the inventory at `slot`, only that singular cell is checked.
  * When removing an item from `slot`, the item in inventory only has to be positioned in such a way that overlaps with `slot`.
  * When adding an item to `slot`, `slot` is treated as the upper left corner (the initial point) of a larger capacity region.<br>
  * <br>
  * The following diagrams demonstrate the coordinate association:<br>
  * `&nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - - - - -`<br>
  * `&nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - r r x - &nbsp;&nbsp;&nbsp; - - - - -`<br>
  * `&nbsp;&nbsp;&nbsp; - - s - - &nbsp;&nbsp;&nbsp; - r r x - &nbsp;&nbsp;&nbsp; - - i i -`<br>
  * `&nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - x x x - &nbsp;&nbsp;&nbsp; - - i i -`<br>
  * `&nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - - - - - &nbsp;&nbsp;&nbsp; - - - - -`<br>
  * ... where 's' is the 1x1 slot,
  * 'r' is the corner of any 2x2 item that can be removed ('x' is a potential affected edge),
  * and 'i' is the region checked for a 2x2 insertion into `slot`.
  */
class InventoryEquipmentSlot(private val slot: Int, private val inv: GridInventory)
    extends OffhandEquipmentSlot(EquipmentSize.Inventory) {
  override def Equipment_=(assignEquipment: Option[Equipment]): Option[Equipment] = {
    assignEquipment match {
      case Some(equip) =>
        val tile = equip.Definition.Tile
        inv.CheckCollisionsVar(slot, tile.Width, tile.Height) match {
          case Success(Nil) => inv.InsertQuickly(slot, equip)
          case _            => ; //TODO we should handle the exception
        }

      case None =>
        inv -= slot
    }
    Equipment
  }

  override def Equipment: Option[Equipment] = {
    inv.CheckCollisionsAsGrid(slot, 1, 1) match {
      case Success(list) =>
        list.headOption match {
          case Some(found) =>
            Some(found.obj)
          case None =>
            None
        }
      case Failure(_) =>
        None
    }
  }
}
