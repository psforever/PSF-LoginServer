// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.TransactionType
import scodec.Codec
import scodec.codecs._

final case class ItemTransactionMessage(terminal_guid : PlanetSideGUID,
                                        transaction_type : TransactionType.Value,
                                        item_page : Int,
                                        item_name : String,
                                        unk1 : Int,
                                        item_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = ItemTransactionMessage
  def opcode = GamePacketOpcode.ItemTransactionMessage
  def encode = ItemTransactionMessage.encode(this)
}

object ItemTransactionMessage extends Marshallable[ItemTransactionMessage] {
  implicit val codec : Codec[ItemTransactionMessage] = (
      ("terminal_guid" | PlanetSideGUID.codec) ::
        ("transaction_type" | TransactionType.codec) ::
        ("item_page" | uint16L) ::
        ("item_name" | PacketHelpers.encodedStringAligned(5)) ::
        ("unk1" | uint8L) ::
        ("item_guid" | PlanetSideGUID.codec)
    ).as[ItemTransactionMessage]
}
