// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.bits._

class HandleGamePacketTest extends Specification {
  //this is the first from a series of SlottedMetaPacket4s; the length field was modified from 12 DC to pass the test
  val base =
    hex"18 D5 96 00 00 BC 8E 00  03 A2 16 5D A4 5F B0 80  00 04 30 40 00 08 30 46  00 4A 00 48 00 02 02 F0  62 1E 80 80 00 00 00 00  00 3F FF CC 0D 40 00 20  00 03 00 27 C3 01 C8 00  00 03 08 00 00 03 FF FF  FF FC A4 04 00 00 62 00  18 02 00 50 00 00 00 00  00 00 00 00 00 00 00 00  00 01 90 01 90 00 C8 00  00 01 00 7E C8 00 C8 00  00 00 5D B0 81 40 00 00  00 00 00 00 00 00 00 00  00 00 02 C0 00 40 83 85  46 86 C7 07 8A 4A 80 70  0C 00 01 98 00 00 01 24  78 70 65 5F 62 61 74 74  6C 65 5F 72 61 6E 6B 5F  31 30 90 78 70 65 5F 6A  6F 69 6E 5F 70 6C 61 74  6F 6F 6E 92 78 70 65 5F  62 61 74 74 6C 65 5F 72  61 6E 6B 5F 31 34 8F 78  70 65 5F 6A 6F 69 6E 5F  6F 75 74 66 69 74 92 78  70 65 5F 62 61 74 74 6C  65 5F 72 61 6E 6B 5F 31  31 91 78 70 65 5F 62 61  74 74 6C 65 5F 72 61 6E  6B 5F 39 91 78 70 65 5F  62 61 74 74 6C 65 5F 72  61 6E 6B 5F 38 92 78 70  65 5F 62 61 74 74 6C 65  5F 72 61 6E 6B 5F 31 33  93 78 70 65 5F 77 61 72  70 5F 67 61 74 65 5F 75  73 61 67 65 91 78 70 65  5F 62 61 74 74 6C 65 5F  72 61 6E 6B 5F 32 92 78  70 65 5F 69 6E 73 74 61  6E 74 5F 61 63 74 69 6F  6E 8E 78 70 65 5F 66 6F  72 6D 5F 73 71 75 61 64  91 78 70 65 5F 62 61 74  74 6C 65 5F 72 61 6E 6B  5F 36 91 78 70 65 5F 62  61 74 74 6C 65 5F 72 61  6E 6B 5F 37 8E 78 70 65  5F 6A 6F 69 6E 5F 73 71  75 61 64 8C 78 70 65 5F  62 69 6E 64 5F 61 6D 73  91 78 70 65 5F 62 61 74  74 6C 65 5F 72 61 6E 6B  5F 35 91 78 70 65 5F 62  69 6E 64 5F 66 61 63 69  6C 69 74"
  val string = hex"00 00 01 CB" ++ base

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case HandleGamePacket(len, data, extra) =>
        len mustEqual 459
        data mustEqual base
        extra mustEqual BitVector.empty
      case _ =>
        ko
    }
  }

  "encode" in {
    val pkt = HandleGamePacket(base)
    val msg = PacketCoding.encodePacket(pkt).require.toByteVector
    msg mustEqual string
  }
}
