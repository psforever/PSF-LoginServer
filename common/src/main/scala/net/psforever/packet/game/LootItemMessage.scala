// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the user right-clicks on a piece of `Equipment`
  * in an inventory that is not his own backpack
  * in an attempt to quick-swap that `Equipment` into his inventory.
  * Examples of this "other" inventory include: corpses, lockers, and vehicle trunks.<br>
  * <br>
  * Compared to `MoveItemMessage`, the source location where `item` currently resides is not provided.
  * Additionally, while the over-all destination is provided, the specific insertion point of the destination is not.
  * @see `MoveItemMessage`
  * @param item_guid the item being taken
  * @param destination_guid where the item will be placed;
  *                         generally, the player is taking the item
  */
final case class LootItemMessage(item_guid : PlanetSideGUID,
                                 destination_guid : PlanetSideGUID
                                ) extends PlanetSideGamePacket {
  type Packet = LootItemMessage
  def opcode = GamePacketOpcode.LootItemMessage
  def encode = LootItemMessage.encode(this)
}

object LootItemMessage extends Marshallable[LootItemMessage] {
  implicit val codec : Codec[LootItemMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("destination_guid" | PlanetSideGUID.codec)
    ).as[LootItemMessage]
}