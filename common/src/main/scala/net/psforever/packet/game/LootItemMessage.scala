// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param item_guid the item being taken
  * @param target_guid what is taking the item;
  *                    in general, usually the player who is doing the taking
  */
final case class LootItemMessage(item_guid : PlanetSideGUID,
                                 target_guid : PlanetSideGUID
                                ) extends PlanetSideGamePacket {
  type Packet = LootItemMessage
  def opcode = GamePacketOpcode.LootItemMessage
  def encode = LootItemMessage.encode(this)
}

object LootItemMessage extends Marshallable[LootItemMessage] {
  implicit val codec : Codec[LootItemMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("target_guid" | PlanetSideGUID.codec)
    ).as[LootItemMessage]
}