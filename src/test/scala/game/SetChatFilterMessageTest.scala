// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class SetChatFilterMessageTest extends Specification {
  val string        = hex"63 05FF80"
  val string_custom = hex"63 05C180"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case SetChatFilterMessage(send, origin, filters) =>
        send mustEqual ChatChannel.Local
        origin mustEqual true
        filters.length mustEqual 9
        filters.head mustEqual ChatChannel.Unknown
        filters(1) mustEqual ChatChannel.Tells
        filters(2) mustEqual ChatChannel.Local
        filters(3) mustEqual ChatChannel.Squad
        filters(4) mustEqual ChatChannel.Outfit
        filters(5) mustEqual ChatChannel.Command
        filters(6) mustEqual ChatChannel.Platoon
        filters(7) mustEqual ChatChannel.Broadcast
        filters(8) mustEqual ChatChannel.SquadLeader
      case _ =>
        ko
    }
  }

  "decode (custom)" in {
    PacketCoding.decodePacket(string_custom).require match {
      case SetChatFilterMessage(send, origin, filters) =>
        send mustEqual ChatChannel.Local
        origin mustEqual true
        filters.length mustEqual 4
        filters.head mustEqual ChatChannel.Unknown
        filters(1) mustEqual ChatChannel.Tells
        filters(2) mustEqual ChatChannel.Broadcast
        filters(3) mustEqual ChatChannel.SquadLeader
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SetChatFilterMessage(
      ChatChannel.Local,
      true,
      List(
        ChatChannel.Unknown,
        ChatChannel.Tells,
        ChatChannel.Local,
        ChatChannel.Squad,
        ChatChannel.Outfit,
        ChatChannel.Command,
        ChatChannel.Platoon,
        ChatChannel.Broadcast,
        ChatChannel.SquadLeader
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (success; same channel listed multiple times)" in {
    val msg = SetChatFilterMessage(
      ChatChannel.Local,
      true,
      List(
        ChatChannel.Unknown,
        ChatChannel.Unknown,
        ChatChannel.Tells,
        ChatChannel.Tells,
        ChatChannel.Local,
        ChatChannel.Squad,
        ChatChannel.Outfit,
        ChatChannel.Command,
        ChatChannel.Platoon,
        ChatChannel.Broadcast,
        ChatChannel.SquadLeader
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (success; out of order)" in {
    val msg = SetChatFilterMessage(
      ChatChannel.Local,
      true,
      List(
        ChatChannel.Squad,
        ChatChannel.Outfit,
        ChatChannel.SquadLeader,
        ChatChannel.Unknown,
        ChatChannel.Command,
        ChatChannel.Platoon,
        ChatChannel.Broadcast,
        ChatChannel.Tells,
        ChatChannel.Local
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (success; custom)" in {
    val msg = SetChatFilterMessage(
      ChatChannel.Local,
      true,
      List(ChatChannel.Unknown, ChatChannel.Tells, ChatChannel.Broadcast, ChatChannel.SquadLeader)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_custom
  }
}
