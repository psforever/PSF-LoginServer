// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Instructs client to update the quantity of an item based on a delta when sent server to client.
  *
  * See also [[QuantityUpdateMessage]]
  *
  * @param item_guid the item to update
  * @param quantity_delta the change in quantity of the item
  */
final case class QuantityDeltaUpdateMessage(item_guid : PlanetSideGUID,
                                            quantity_delta : Int)
  extends PlanetSideGamePacket {
  type Packet = QuantityDeltaUpdateMessage
  def opcode = GamePacketOpcode.QuantityDeltaUpdateMessage
  def encode = QuantityDeltaUpdateMessage.encode(this)
}

object QuantityDeltaUpdateMessage extends Marshallable[QuantityDeltaUpdateMessage] {
  implicit val codec : Codec[QuantityDeltaUpdateMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("quantity_delta" | int32L)
    ).as[QuantityDeltaUpdateMessage]
}
