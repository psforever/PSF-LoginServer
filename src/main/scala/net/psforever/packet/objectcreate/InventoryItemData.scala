// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.types.PlanetSideGUID
import scodec.Codec

/**
  * Mask the use of `InternalSlot` using a fake class called an `InventoryItemData`.
  */
object InventoryItemData {

  /**
    * Constructor for creating an `InventoryItemData`.
    * @param guid the GUID this object will be assigned
    * @param slot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param obj the data used as representation of the object to be constructed
    * @return an `InventoryItemData` object
    */
  def apply(objClass: Int, guid: PlanetSideGUID, slot: Int, obj: ConstructorData): InventoryItem =
    InternalSlot(objClass, guid, slot, obj)

  /**
    * Alias `InventoryItemData` to `InternalSlot`.
    */
  type InventoryItem = InternalSlot

  /**
    * A `Codec` for `0x17` `ObjectCreateMessage` data.
    */
  val codec: Codec[InventoryItem] = InternalSlot.codec

  /**
    * A `Codec` for `0x18` `ObjectCreateDetailedMessage` data.
    */
  val codec_detailed: Codec[InventoryItem] = InternalSlot.codec_detailed
}
