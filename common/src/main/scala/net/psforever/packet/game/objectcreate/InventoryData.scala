// Copyright (c) 2016 PSForever.net to present
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
  * Exploration:<br>
  * 4u of ignored bits are tagged onto the end of this field for purposes of finding four missing bits of stream length.
  * The rest of the encoding is valid.
  * Conditions must certainly decide whether these bits are present or not.
  * @param unk1 na;
  *             `true` to mark the start of the inventory data?
  * @param unk2 na
  * @param unk3 na
  * @param contents the actual items in the inventory;
  *                  holster slots are 0-4;
  *                  an inaccessible slot is 5;
  *                  internal capacity is 6-`n`, where `n` is defined by exosuit type and is mapped into a grid
  */
case class InventoryData(unk1 : Boolean,
                         unk2 : Boolean,
                         unk3 : Boolean,
                         contents : List[InventoryItem]) extends StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    //three booleans, the 4u and the 8u length field
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
      case u1 :: _ :: u2 :: u3 :: ctnt :: _ :: HNil =>
        InventoryData(u1, u2, u3, ctnt)
    },
    {
      case InventoryData(u1, u2, u3, ctnt) =>
        u1 :: ctnt.size :: u2 :: u3 :: ctnt :: () :: HNil
    }
  ).as[InventoryData]
}
