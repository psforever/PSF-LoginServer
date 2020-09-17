// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class MultiPacketTest extends Specification {
  val string =
    hex"00 03 04 00 15 13 23 3A 00 09 03 E3 00 19 16 6D 56 05 68 05 40 A0 EF 45 00 15 0E 44 00 A0 A2 41 00 00 0F 88 00 06 E4 C0  60 00 00 00 15 E4 32 40 74 72 61 69 6E 69 6E 67  5F 77 65 61 70 6F 6E 73 30 31 13 BD 68 05 53 F6  EF 90 D1 6E 03 14 FE 78 8C 20 1C C0 00 00 1F 00  09 03 E4 6D 56 05 68 05 40 A0 EF 45 00 15 0E 44 30 89 A1 41 00 00 0F 8A 01 00 04 18 EF 80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case MultiPacket(data) =>
        data.size mustEqual 4
        data(0) mustEqual hex"00151323"
        data(
          1
        ) mustEqual hex"000903e30019166d5605680540a0ef4500150e4400a0a24100000f880006e4c06000000015e43240747261696e696e675f776561706f6e733031"
        data(2) mustEqual hex"bd680553f6ef90d16e0314fe788c201cc00000"
        data(3) mustEqual hex"000903e46d5605680540a0ef4500150e443089a14100000f8a01000418ef80"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = MultiPacket(
      Vector(
        hex"00151323",
        hex"000903e30019166d5605680540a0ef4500150e4400a0a24100000f880006e4c06000000015e43240747261696e696e675f776561706f6e733031",
        hex"bd680553f6ef90d16e0314fe788c201cc00000",
        hex"000903e46d5605680540a0ef4500150e443089a14100000f8a01000418ef80"
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
