// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

/**
  * A "tile" represents the size of the icon placard that is used by `Equipment` when placed into an inventory or visible slot.
  * It is also used by some `ObjectDefinition`s to pass information about the size of an inventory itself.
  * @param width the width of the tile
  * @param height the height of the tile
  * @throws IllegalArgumentException if either the width or the height are less than zero
  */
class InventoryTile(val width : Int, val height : Int) {
  if(width < 0 || height < 0)
    throw new IllegalArgumentException(s"tile has no area - width: $width, height: $height")

  def Width : Int = width

  def Height : Int = height
}

object InventoryTile {
  final val None = InventoryTile(0,0) //technically invalid; used to indicate a vehicle with no trunk
  final val Tile11 = InventoryTile(1,1) //placeholder size
  final val Tile22 = InventoryTile(2,2) //grenades, boomer trigger
  final val Tile23 = InventoryTile(2,3) //canister ammo
  final val Tile42 = InventoryTile(4,2) //medkit
  final val Tile33 = InventoryTile(3,3) //ammo box, pistols, ace
  final val Tile44 = InventoryTile(4,4) //large ammo box
  final val Tile55 = InventoryTile(5,5) //bfr ammo box
  final val Tile63 = InventoryTile(6,3) //rifles
  final val Tile93 = InventoryTile(9,3) //long-body weapons

  def apply(w : Int, h : Int) : InventoryTile = {
    new InventoryTile(w, h)
  }
}
