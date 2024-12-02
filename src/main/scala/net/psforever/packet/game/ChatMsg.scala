// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs._
import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.ChatMessageType
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

/*
For colors, type '/#n' before text, where `n` is one of the following hexadecimal numbers:
  0 white
  1 black
  2 cyan
  3 yellow
  4 green
  5 light blue
  6 brown
  7 violet
  8 magneta
  9 purple
  a purple
  b yellow green
  c blue
  d light pink
  e light green
  f beige
All other options result in white text.
*/

/**
  * Instructs client to display and/or process a chat message/command when sent server to client.
  * Instructs server to route and/or process a chat message/command when sent client to server.
  *
  * @param messageType the type of the chat message (CMT)
  * @param wideContents whether the contents contains wide characters or not. This is
  *                     required because Java/Scala have one String type with a charset
  *                     of UTF-16. Therefore, there is no way at runtime to determine the
  *                     charset of String.
  * @param recipient identifies the recipient of the message, such as in a tell (occasionally used as "sender" instead i.e. /note)
  * @param contents the textual contents of the message
  * @param note only used when the message is of note type
  */
final case class ChatMsg(
    messageType: ChatMessageType,
    wideContents: Boolean,
    recipient: String,
    contents: String,
    note: Option[String]
) extends PlanetSideGamePacket {

  // Prevent usage of the Note field unless the message is of type note
  if (messageType == ChatMessageType.CMT_NOTE)
    assert(note.isDefined, "Note contents required")
  else
    assert(note.isEmpty, "Note contents found, but message type isnt Note")

  type Packet = ChatMsg
  def opcode: Type = GamePacketOpcode.ChatMsg
  def encode: Attempt[BitVector] = ChatMsg.encode(this)
}

object ChatMsg extends Marshallable[ChatMsg] {
  def apply(messageType: ChatMessageType, contents: String): ChatMsg =
    ChatMsg(messageType, wideContents=false, recipient="", contents, note=None)

  implicit val codec: Codec[ChatMsg] = (("messagetype" | ChatMessageType.codec) >>:~ { messagetype_value =>
    (("has_wide_contents" | bool) >>:~ { isWide =>
      ("recipient" | PacketHelpers.encodedWideStringAligned(7)) ::
        newcodecs.binary_choice(
          isWide,
          "contents" | PacketHelpers.encodedWideString,
          "contents" | PacketHelpers.encodedString
        )
    }) :+
      conditional(messagetype_value == ChatMessageType.CMT_NOTE, "note_contents" | PacketHelpers.encodedWideString)
  }).as[ChatMsg]
}
