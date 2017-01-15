// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * A representation of an item in an avatar's inventory.
  * Reliance on `InternalSlot` indicates that this item is applicable to the same implicit parent-child relationship.
  * (That is, its parent object will be clarified by the containing element, e.g., the inventory or its owner.)
  * Unwinding inventory items into individual standard `ObjectCreateMessage` packet data is entirely possible.<br>
  * <br>
  * This intermediary object is primarily intended to mask external use of `InternalSlot`, as specified by the class.
  * @param item the object in inventory
  * @see InternalSlot
  */
final case class InventoryItem(item : InternalSlot) extends StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = item.bitsize
}

object InventoryItem extends Marshallable[InventoryItem] {
  /**
    * An abbreviated constructor for creating an `InventoryItem` without interacting with `InternalSlot` directly.
    * @param objClass the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param obj the constructor data
    * @return an InventoryItem
    */
  def apply(objClass : Int, guid : PlanetSideGUID, parentSlot : Int, obj : ConstructorData) : InventoryItem =
    InventoryItem(InternalSlot(objClass, guid, parentSlot, obj))

  implicit val codec : Codec[InventoryItem] = (
    "item" | InternalSlot.codec
  ).as[InventoryItem]
}
