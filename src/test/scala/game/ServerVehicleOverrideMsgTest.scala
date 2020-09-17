// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ServerVehicleOverrideMsgTest extends Specification {
  val string1 = hex"4E C0 0C0 00000000 0"
  val string2 = hex"4E 10 050 0"

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case ServerVehicleOverrideMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual true
        u2 mustEqual true
        u3 mustEqual false
        u4 mustEqual false
        u5 mustEqual 0
        u6 mustEqual 0
        u7 mustEqual 12
        u8.isDefined mustEqual true
        u8.get mustEqual 0L
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case ServerVehicleOverrideMsg(u1, u2, u3, u4, u5, u6, u7, u8) =>
        u1 mustEqual false
        u2 mustEqual false
        u3 mustEqual false
        u4 mustEqual true
        u5 mustEqual 0
        u6 mustEqual 0
        u7 mustEqual 5
        u8.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = ServerVehicleOverrideMsg(true, true, false, false, 0, 0, 12, Some(0L))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = ServerVehicleOverrideMsg(false, false, false, true, 0, 0, 5, None)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}
