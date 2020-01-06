// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.equipment.Equipment
import net.psforever.types.PlanetSideGUID

/**
  * Represent the image placard that is used to visually and spatially manipulate an item placed into the grid-like inventory.
  * The unofficial term for this placard (the size of the placard) is a "tile."
  * The size of the tile is usually fixed but the origin point of the tile can be changed.
  * @param obj the item being placed into the inventory grid
  * @param start the index of the upper-left square of the item's tile
  */
class InventoryItem(val obj : Equipment, var start : Int = 0) {
  //TODO eventually move this object from storing the item directly to just storing its GUID?
  def GUID : PlanetSideGUID = obj.GUID
}

object InventoryItem {
  def apply(obj : Equipment, start : Int) : InventoryItem = {
    new InventoryItem(obj, start)
  }

  def unapply(entry : InventoryItem) : Option[(Equipment, Int)] = {
    Some((entry.obj, entry.start))
  }
}
