// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

case class InventoryItem(item : InternalSlot)

object InventoryItem extends Marshallable[InventoryItem] {
  implicit val codec : Codec[InventoryItem] = (
    "item" | InternalSlot.codec
    ).as[InventoryItem]
}
