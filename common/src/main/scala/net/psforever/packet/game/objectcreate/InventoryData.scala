// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the inventory portion of `ObjectCreateMessage` packet data for avatars.<br>
  * <br>
  * The inventory is a temperamental thing.
  * Items placed into the inventory must follow their proper encoding schematics to the letter.
  * No values are allowed to be misplaced and no unexpected regions of data can be discovered.
  * If there is even a minor failure, the whole of the inventory will fail to translate.<br>
  * <br>
  * Under the official servers, when a new character was generated, the inventory encoded as `0x1C`.
  * This inventory had no size field, no contents, and an indeterminate number of values.
  * This format is no longer supported.
  * Going forward, an empty inventory - approximately `0x10000` - should be used as substitute.<br>
  * <br>
  * Exploration:<br>
  * 4u of ignored bits have been added to the end of the inventory to make up for missing stream length.
  * They do not actually seem to be part of the inventory.
  * Are these bits always at the end of the packet data and what is the significance?
  * @param unk1 na;
  *             `true` to mark the start of the inventory data?
  *             is explicitly declaring the bit necessary when it always seems to be `true`?
  * @param unk2 na
  * @param unk3 na
  * @param contents the actual items in the inventory;
  *                  holster slots are 0-4;
  *                  an inaccessible slot is 5;
  *                  internal capacity is 6-`n`, where `n` is defined by exosuit type and is mapped into a grid
  */
final case class InventoryData(unk1 : Boolean,
                               unk2 : Boolean,
                               unk3 : Boolean,
                               contents : List[InventoryItem]) extends StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    //three booleans, the 4u extra, and the 8u length field
    val base : Long = 15L
    //length of all items in inventory
    var invSize : Long = 0L
    for(item <- contents) {
      invSize += item.bitsize
    }
    base + invSize
  }
}

object InventoryData extends Marshallable[InventoryData] {
  implicit val codec : Codec[InventoryData] = (
    ("unk1" | bool) ::
      (("len" | uint8L) >>:~ { len =>
        ("unk2" | bool) ::
          ("unk3" | bool) ::
          ("contents" | PacketHelpers.listOfNSized(len, InventoryItem.codec)) ::
          ignore(4)
      })
    ).xmap[InventoryData] (
    {
      case u1 :: _ :: a :: b :: ctnt :: _ :: HNil =>
        InventoryData(u1, a, b, ctnt)
    },
    {
      case InventoryData(u1, a, b, ctnt) =>
        u1 :: ctnt.size :: a :: b :: ctnt :: () :: HNil
    }
  ).as[InventoryData]
}
