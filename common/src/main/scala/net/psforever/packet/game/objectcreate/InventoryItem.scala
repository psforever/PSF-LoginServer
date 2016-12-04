// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

case class InventoryItem(item : InternalSlot,
                         na : Option[Boolean] = None)

object InventoryItem extends Marshallable[InventoryItem] {
  implicit val codec : Codec[InventoryItem] = (
    "item" | InternalSlot.codec >>:~ { item =>
      conditional(item.obj.isDefined && item.obj.get.isInstanceOf[WeaponData], bool).hlist
    }
    ).as[InventoryItem]
}
