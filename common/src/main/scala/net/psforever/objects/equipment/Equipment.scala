// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.inventory.InventoryTile

/**
  * `Equipment` is anything that can be:
  * placed into a slot of a certain "size";
  * and, placed into an inventory system;
  * and, special carried (like a lattice logic unit);
  * and, dropped on the ground in the game world and render where it was deposited.
  */
abstract class Equipment extends PlanetSideGameObject {
  def Size : EquipmentSize.Value = Definition.Size

  def Tile : InventoryTile = Definition.Tile

  def Definition : EquipmentDefinition

  override def toString : String = {
    Equipment.toString(this)
  }
}

object Equipment {
  def toString(obj : Equipment) : String = {
    obj.Definition.Name
  }
}
