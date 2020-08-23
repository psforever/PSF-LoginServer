// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

/**
  * A "tile" represents the size of the icon placard that is used by `Equipment` when placed into an inventory or visible slot.
  * It is also used by some `ObjectDefinition`s to pass information about the size of an inventory itself.
  * @param width the width of the tile
  * @param height the height of the tile
  * @throws IllegalArgumentException if either the width or the height are less than zero
  */
class InventoryTile(private val width: Int, private val height: Int) {
  if (width < 0 || height < 0)
    throw new IllegalArgumentException(s"tile has no area - width: $width, height: $height")

  def Width: Int = width

  def Height: Int = height
}

object InventoryTile {
  final val None     = InventoryTile(0, 0)   //technically invalid; used to indicate a vehicle with no trunk
  final val Tile11   = InventoryTile(1, 1)   //occasional placeholder
  final val Tile22   = InventoryTile(2, 2)   //grenades, boomer trigger
  final val Tile23   = InventoryTile(2, 3)   //canister ammo
  final val Tile42   = InventoryTile(4, 2)   //medkit
  final val Tile33   = InventoryTile(3, 3)   //ammo box, pistols, ace
  final val Tile44   = InventoryTile(4, 4)   //large ammo box
  final val Tile55   = InventoryTile(5, 5)   //bfr ammo box
  final val Tile66   = InventoryTile(6, 6)   //infiltration suit inventory
  final val Tile63   = InventoryTile(6, 3)   //rifles
  final val Tile93   = InventoryTile(9, 3)   //long-body weapons
  final val Tile96   = InventoryTile(9, 6)   //standard exo-suit inventory
  final val Tile99   = InventoryTile(9, 9)   //agile exo-suit inventory
  final val Tile1107 = InventoryTile(11, 7)  //uncommon small trunk capacity - phantasm
  final val Tile1111 = InventoryTile(11, 11) //common small trunk capacity
  final val Tile1209 = InventoryTile(12, 9)  //reinforced exo-suit inventory
  final val Tile1511 = InventoryTile(15, 11) //common medium trunk capacity
  final val Tile1515 = InventoryTile(15, 15) //common large trunk capacity
  final val Tile1611 = InventoryTile(16, 11) //uncommon medium trunk capacity - vulture
  final val Tile1612 = InventoryTile(16, 12) //MAX; uncommon medium trunk capacity - lodestar
  final val Tile1816 = InventoryTile(18, 16) //uncommon massive trunk capacity - galaxy_gunship
  final val Tile2016 = InventoryTile(20, 16) //uncommon massive trunk capacity - apc

  def apply(w: Int, h: Int): InventoryTile = {
    new InventoryTile(w, h)
  }
}
