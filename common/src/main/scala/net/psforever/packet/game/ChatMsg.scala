// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs._
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.ChatMessageType
import scodec.Codec
import scodec.codecs._

/**
  * Instructs client to display and/or process a chat message/command when sent server to client.
  * Instructs server to route and/or process a chat message/command when sent client to server.
  *
  * @param messagetype the type of the chat message (CMT)
  * @param has_wide_contents whether the contents contains wide characters or not
  * @param recipient identifies the recipient of the message, such as in a tell (occasionally used as "sender" instead i.e. /note)
  * @param contents the textual contents of the message
  * @param note_contents only present when the message is of note type
  */
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
      conditional(messagetype_value == ChatMessageType.CMT_NOTE, ("note_contents" | PacketHelpers.encodedWideString))
    }).as[ChatMsg]
}
