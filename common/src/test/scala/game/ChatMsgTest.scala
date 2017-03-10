// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.ChatMessageType
import scodec.bits._

class ChatMsgTest extends Specification {
  val string_local = hex"12 1A C000 83610062006300"
  val string_tell  = hex"12 20 C180640065006600 83610062006300"

  "decode" in {
    PacketCoding.DecodePacket(string_local).require match {
      case ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
        messagetype mustEqual ChatMessageType.CMT_OPEN
        has_wide_contents mustEqual true
        recipient mustEqual ""
        contents mustEqual "abc"
        note_contents mustEqual None
      case _ =>
        ko
    }

    PacketCoding.DecodePacket(string_tell).require match {
      case ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
        messagetype mustEqual ChatMessageType.CMT_TELL
        has_wide_contents mustEqual true
        recipient mustEqual "def"
        contents mustEqual "abc"
        note_contents mustEqual None
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg_local = ChatMsg(ChatMessageType.CMT_OPEN, true, "", "abc", None)
    val pkt_local = PacketCoding.EncodePacket(msg_local).require.toByteVector

    pkt_local mustEqual string_local

    val msg_tell = ChatMsg(ChatMessageType.CMT_TELL, true, "def", "abc", None)
    val pkt_tell = PacketCoding.EncodePacket(msg_tell).require.toByteVector

    pkt_tell mustEqual string_tell
  }

  "allow and disallow note" in {
    ChatMsg(ChatMessageType.CMT_ARMOR,
      false,
      "DontCare", "DontCare", Some("Should be here")) must throwA[AssertionError]
    ChatMsg(ChatMessageType.CMT_NOTE,
      false,
      "DontCare", "DontCare", None) must throwA[AssertionError]
  }
}
