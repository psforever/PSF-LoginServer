// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class FriendsResponseTest extends Specification {
  val stringOneFriend = hex"73 61 8C 60 4B007500720074004800650063007400690063002D004700 00"
  val stringManyFriends = hex"73 01 AC 48 4100 6E00 6700 6500 6C00 6C00 6F00 2D00 5700 47 00 7400 6800 6500 7000 6800 6100 7400 7400 7000 6800 7200 6F00 6700 6700 46 80 4B00 6900 6D00 7000 6F00 7300 7300 6900 6200 6C00 6500 3100 3200 45 00 5A00 6500 6100 7200 7400 6800 6C00 6900 6E00 6700 46 00 4B00 7500 7200 7400 4800 6500 6300 7400 6900 6300 2D00 4700 00"
  val stringShort = hex"73 81 80"

  "decode (one friend)" in {
    PacketCoding.DecodePacket(stringOneFriend).require match {
      case FriendsResponse(action, unk2, unk3, unk4, list) =>
        action mustEqual 3
        unk2 mustEqual 0
        unk3 mustEqual true
        unk4 mustEqual true
        list.size mustEqual 1
        list.head.name mustEqual "KurtHectic-G"
        list.head.online mustEqual false
      case _ =>
        ko
    }
  }

  "decode (multiple friends)" in {
    PacketCoding.DecodePacket(stringManyFriends).require match {
      case FriendsResponse(action, unk2, unk3, unk4, list) =>
        action mustEqual 0
        unk2 mustEqual 0
        unk3 mustEqual true
        unk4 mustEqual true
        list.size mustEqual 5
        list.head.name mustEqual "Angello-W"
        list.head.online mustEqual false
        list(1).name mustEqual "thephattphrogg"
        list(1).online mustEqual false
        list(2).name mustEqual "Kimpossible12"
        list(2).online mustEqual false
        list(3).name mustEqual "Zearthling"
        list(3).online mustEqual false
        list(4).name mustEqual "KurtHectic-G"
        list(4).online mustEqual false
      case _ =>
        ko
    }
  }

  "decode (short)" in {
    PacketCoding.DecodePacket(stringShort).require match {
      case FriendsResponse(action, unk2, unk3, unk4, list) =>
        action mustEqual 4
        unk2 mustEqual 0
        unk3 mustEqual true
        unk4 mustEqual true
        list.size mustEqual 0
      case _ =>
        ko
    }
  }

  "encode (one friend)" in {
    val msg = FriendsResponse(3, 0, true, true,
      Friend("KurtHectic-G", false) ::
        Nil)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringOneFriend
  }

  "encode (multiple friends)" in {
    val msg = FriendsResponse(0, 0, true, true,
      Friend("Angello-W", false) ::
        Friend("thephattphrogg", false) ::
        Friend("Kimpossible12", false) ::
        Friend("Zearthling", false) ::
        Friend("KurtHectic-G", false) ::
        Nil)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringManyFriends
  }

  "encode (short)" in {
    val msg = FriendsResponse(4, 0, true, true)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringShort
  }
}
