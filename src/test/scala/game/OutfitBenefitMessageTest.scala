// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.packets.OutfitBenefitMessage
import scodec.bits._

/**
  * The `OutfitBenefitMessage` has never been captured from the stock servers;
  * the wire format below is derived from the client's own decoder
  * (`OutfitBenefitMessage::Decode` at `0x8a4cc0` of the 3.15.84 binary):
  * a flat record of four 32-bit values.
  */
class OutfitBenefitMessageTest extends Specification {

  "encode and decode round trip" in {
    val msg = OutfitBenefitMessage(outfit_id = 41593365L, unk1 = 1000L, unk2 = 0x12L, unk3 = 0xffffffffL)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    PacketCoding.decodePacket(pkt).require mustEqual msg
  }

  "encode and decode round trip with all-zero values" in {
    val msg = OutfitBenefitMessage(outfit_id = 0L, unk1 = 0L, unk2 = 0L, unk3 = 0L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    PacketCoding.decodePacket(pkt).require mustEqual msg
  }

  "the opcode is 0xca" in {
    val pkt = PacketCoding.encodePacket(OutfitBenefitMessage(1L, 2L, 3L, 4L)).require.toByteVector
    (pkt.head & 0xff) mustEqual 0xca
    // four 32-bit little-endian words follow the opcode
    pkt.tail mustEqual ByteVector(1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0)
  }
}
