// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package game

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.packets.WorldPingMessage
import org.specs2.mutable._
import scodec.bits._

class WorldPingMessageTest extends Specification {
  // 9 byte raw UDP packet:
  // Opcode: 0x1A
  // World Index: 0
  // Send Timestamp: 14000
  val string = hex"1a 00 00 00 00 b0 36 00 00"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case WorldPingMessage(worldIndex, sendTimestamp) =>
        worldIndex mustEqual 0L
        sendTimestamp mustEqual 14000L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WorldPingMessage(0L, 14000L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  // non-zero world index and large tick count:
  // Opcode: 0x1A
  // World Index: 2
  // Send Timestamp: 305419896
  val string2 = hex"1a 02 00 00 00 78 56 34 12"

  "decode2" in {
    PacketCoding.decodePacket(string2).require match {
      case WorldPingMessage(worldIndex, sendTimestamp) =>
        worldIndex mustEqual 2L
        sendTimestamp mustEqual 305419896L
      case _ =>
        ko
    }
  }

  "encode2" in {
    val msg = WorldPingMessage(2L, 305419896L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}
