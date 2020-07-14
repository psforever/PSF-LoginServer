// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import InventoryItemData._
import net.psforever.packet.PacketHelpers
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the inventory portion of `ObjectCreate*Message` packet data for avatars.<br>
  * <br>
  * The inventory is a temperamental thing.
  * Items placed into the inventory must follow their proper encoding schematics to the letter.
  * The slot number refers to the position occupied by the item.
  * In icon format, all-encompassing slots are absolute positions; and, grid-distributed icons use the upper-left corner.
  * No values are allowed to be misplaced and no unexpected regions of data can be discovered.
  * If there is even a minor failure, the remainder of the inventory will fail to translate.<br>
  * <br>
  * Inventories are usually prefaced with a single bit value not accounted for here to switch them "on."
  * @param contents the items in the inventory
  * @see `InventoryItemData`
  */
final case class InventoryData(contents: List[InventoryItem] = List.empty) extends StreamBitSize {
  override def bitsize: Long = {
    val base: Long    = InventoryData.BaseSize
    var invSize: Long = 0L //length of all items in inventory
    for (item <- contents) {
      invSize += item.bitsize
    }
    base + invSize
  }
}

object InventoryData {
  final val BaseSize: Long = 10L //8u + 1u + 1u

  /**
    * The primary `Codec` that parses the common format for an inventory `List`.
    * @param itemCodec a `Codec` that describes each of the contents of the list
    * @return an `InventoryData` object, or a `BitVector`
    */
  def codec(itemCodec: Codec[InventoryItem]): Codec[InventoryData] =
    (
      uint8L >>:~ { len =>
        uint2L ::
          ("contents" | PacketHelpers.listOfNSized(len, itemCodec))
      }
    ).xmap[InventoryData](
      {
        case _ :: 0 :: c :: HNil =>
          InventoryData(c)
      },
      {
        case InventoryData(c) =>
          c.size :: 0 :: c :: HNil
      }
    )

  /**
    * A `Codec` for `0x17` `ObjectCreateMessage` data.
    */
  val codec: Codec[InventoryData] = codec(InventoryItemData.codec)

  /**
    * A `Codec` for `0x18` `ObjectCreateDetailedMessage` data.
    */
  val codec_detailed: Codec[InventoryData] = codec(InventoryItemData.codec_detailed)
}
