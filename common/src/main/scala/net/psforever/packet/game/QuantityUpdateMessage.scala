// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Instructs client to update the quantity of an item when sent server to client.
  *
  * See also [[QuantityDeltaUpdateMessage]]
  *
  * @param item_guid the item to update
  * @param quantity the quantity to update the item to
  */
final case class QuantityUpdateMessage(item_guid : PlanetSideGUID,
                                       quantity : Int)
  extends PlanetSideGamePacket {
  type Packet = QuantityUpdateMessage
  def opcode = GamePacketOpcode.QuantityUpdateMessage
  def encode = QuantityUpdateMessage.encode(this)
}

object QuantityUpdateMessage extends Marshallable[QuantityUpdateMessage] {
  implicit val codec : Codec[QuantityUpdateMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("quantity" | int32L)
    ).as[QuantityUpdateMessage]
}
