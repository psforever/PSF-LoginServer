// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.OffhandEquipmentSlot
import net.psforever.objects.equipment.{Equipment, EquipmentSize}

/**
  * A slot-like interface for a specific grid position in an inventory.
  * The size is bound to anything that can be stowed, which encompasses most all `Equipment`.
  * Furthermore, rather than operating on a fixed-size slot, this "slot" represents an inventory region that either includes `slot` or starts at `slot`.
  * An object added to the underlying inventory from here can only be added with its initial point at `slot`.
  * An object found at `slot`, however, can be removed even if the starting cell is prior to `slot.`
  */
class InventoryEquipmentSlot(private val slot : Int, private val inv : GridInventory) extends OffhandEquipmentSlot(EquipmentSize.Inventory) {
  /**
    * Attempt to stow an item into the inventory at the given position.
    * @param assignEquipment the change in `Equipment` for this slot
    * @return the `Equipment` in this slot
    */
  override def Equipment_=(assignEquipment : Option[Equipment]) : Option[Equipment] = {
    assignEquipment match {
      case Some(equip) =>
        inv += slot -> equip
      case None =>
        inv -= slot
    }
    Equipment
  }

  /**
    * Determine what `Equipment`, if any, is stowed in the inventory in the given position.
    * @return the `Equipment` in this slot
    */
  override def Equipment : Option[Equipment] = {
    inv.Items.find({ case ((_, item : InventoryItem)) => item.start == slot }) match {
      case Some((_, item : InventoryItem)) =>
        Some(item.obj)
      case None =>
        None
    }
  }
}

