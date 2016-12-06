// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs._

case class InventoryData(unk1 : Boolean,
                         size : Int,
                         unk2 : Boolean){//,
                         //inv : List[InventoryItem]) {
  def bsize : Long = {
    10L
  }
}

object InventoryData extends Marshallable[InventoryData] {
  implicit val codec : Codec[InventoryData] = (
    ("unk1" | bool) ::
      (("size" | uint8L) >>:~ { len =>
        ("unk2" | bool).hlist// ::
          //("inv" | PacketHelpers.listOfNSized(len, InventoryItem.codec))
      })
    ).as[InventoryData]
}
