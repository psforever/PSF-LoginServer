// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Represent an item in inventory.<br>
  * <br>
  * Note the use of `InternalSlot` to indicate the implicit parent ownership of the resulting item.
  * Unwinding inventory items into individual standard `ObjectCreateMessage` packet data is entirely possible.
  * @param item the object in inventory
  * @param na the user should not have to worry about this potential bit;
  *           it follows after weapon entries, allegedly
  */
case class InventoryItem(item : InternalSlot,
                         na : Option[Boolean] = None) {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  def bitsize : Long = {
    //item
    val first : Long = item.bitsize
    //trailing bit
    val second : Long = if(na.isDefined) 1L else 0L
    first + second
  }
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
  def apply(objClass : Int, guid : PlanetSideGUID, parentSlot : Int, obj : ConstructorData) : InventoryItem = {
    val isWep = if(obj.isInstanceOf[WeaponData]) Some(false) else None
    //TODO is this always Some(false)?
    InventoryItem(InternalSlot(objClass, guid, parentSlot, obj), isWep)
  }

  /**
    * Determine whether the allocated item is a weapon.
    * @param itm the inventory item
    * @return true, if the item is a weapon; false, otherwise
    */
  def wasWeapon(itm : InternalSlot) : Boolean = itm.obj.isInstanceOf[WeaponData]

  implicit val codec : Codec[InventoryItem] = (
    ("item" | InternalSlot.codec) >>:~ { item =>
      conditional(wasWeapon(item), bool).hlist
    }
  ).as[InventoryItem]
}
