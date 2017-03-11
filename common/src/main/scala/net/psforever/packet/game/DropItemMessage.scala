// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player's intent is to put an item down on the ground.<br>
  * <br>
  * When a player drops an item, it normally appears right under their feet (where they are standing).
  * This part of the ground is chosen because it should be the stable.
  * Also, those coordinates belonging to the player are the most accessible.
  * This process, however, is not automatic.
  * The server determines the exact position where the item gets placed.<br>
  * <br>
  * This packet is complemented by an `ObjectDetachMessage` packet from the server that performs the actual "dropping."
  * @param item_guid the item to be dropped
  */
final case class DropItemMessage(item_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = DropItemMessage
  def opcode = GamePacketOpcode.DropItemMessage
  def encode = DropItemMessage.encode(this)
}

object DropItemMessage extends Marshallable[DropItemMessage] {
  implicit val codec : Codec[DropItemMessage] = (
      "item_guid" | PlanetSideGUID.codec
    ).as[DropItemMessage]
}
