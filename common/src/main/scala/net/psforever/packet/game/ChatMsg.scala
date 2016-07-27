// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs._
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.ChatMessageType
import scodec.Codec
import scodec.codecs._

final case class ChatMsg(messagetype : ChatMessageType.Value,
                         has_wide_contents : Boolean,
                         recipient : String,
                         contents : String,
                         note_contents : Option[String])
  extends PlanetSideGamePacket {
  type Packet = ChatMsg
  def opcode = GamePacketOpcode.ChatMsg
  def encode = ChatMsg.encode(this)
}

object ChatMsg extends Marshallable[ChatMsg] {
  implicit val codec : Codec[ChatMsg] = (
    ("messagetype" | ChatMessageType.codec) >>:~ { messagetype_value =>
      (("has_wide_contents" | bool) >>:~ { has_wide_contents_value =>
        ("recipient" | PacketHelpers.encodedWideStringAligned(7)) ::
        newcodecs.binary_choice(has_wide_contents_value, ("contents" | PacketHelpers.encodedWideString), ("contents" | PacketHelpers.encodedString))
      }) :+
      conditional(messagetype_value == ChatMessageType.Note, ("note_contents" | PacketHelpers.encodedWideString))
    }).as[ChatMsg]
}
