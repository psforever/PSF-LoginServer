// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.ChatMessageType
import scodec.Codec
import scodec.codecs._

final case class ChatMsg(channel : ChatMessageType.Value,
                         unk1 : Boolean,
                         recipient : String,
                         contents : String)
  extends PlanetSideGamePacket {
  type Packet = ChatMsg
  def opcode = GamePacketOpcode.ChatMsg
  def encode = ChatMsg.encode(this)
}

object ChatMsg extends Marshallable[ChatMsg] {
  implicit val codec : Codec[ChatMsg] = (
    ("messagetype" | ChatMessageType.codec) ::
      ("unk1" | bool) ::
      ("recipient" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("contents" | PacketHelpers.encodedWideString)
    ).as[ChatMsg]
}
