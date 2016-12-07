// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._
import shapeless.{::,HNil}

/**
  * A representation of the inventory portion of `ObjectCreateMessage` packet data for avatars.<br>
  * <br>
  * Unfortunately, the inventory is a fail-fast greedy thing.
  * Any format discrepancies will cause it to fail and that will cause character encoding to fail as well.
  * Care should be taken that all possible item encodings are representable.
  * @param unk1 na;
  *             always `true` to mark the start of the inventory data?
  * @param unk2 na
  * @param contents the actual items in the inventory;
  *                  holster slots are 0-4;
  *                  an inaccessible slot is 5;
  *                  internal capacity is 6-`n`, where `n` is defined by exosuit type and is mapped into a grid
  */
case class InventoryData(unk1 : Boolean,
                         unk2 : Boolean,
                         contents : Vector[InventoryItem]) {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  def bitsize : Long = {
    //two booleans and the 8-bit length field
    val first : Long = 10L
    //length of all items in inventory
    var second : Long = 0L
    for(item <- contents) {
      second += item.bitsize
    }
    first + second
  }
}

object InventoryData extends Marshallable[InventoryData] {
  implicit val codec : Codec[InventoryData] = (
    ("unk1" | bool) ::
      ("len" | uint8L) ::
      ("unk2" | bool) ::
      ("contents" | vector(InventoryItem.codec))
    ).xmap[InventoryData] (
    {
      case u1 :: _ :: u2 :: vector :: HNil =>
        InventoryData(u1, u2, vector)
    },
    {
      case InventoryData(u1, u2, vector) =>
        u1 :: vector.length :: u2 :: vector :: HNil
    }
  ).as[InventoryData]
}
