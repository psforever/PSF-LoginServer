// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class AvatarSearchCriteriaMessageTest extends Specification {
  val string = hex"64 C604 00 00 00 00 00 00"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case AvatarSearchCriteriaMessage(unk1, unk2) =>
        unk1 mustEqual PlanetSideGUID(1222)
        unk2.length mustEqual 6
        unk2.head mustEqual 0
        unk2(1) mustEqual 0
        unk2(2) mustEqual 0
        unk2(3) mustEqual 0
        unk2(4) mustEqual 0
        unk2(5) mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarSearchCriteriaMessage(PlanetSideGUID(1222), List(0, 0, 0, 0, 0, 0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (failure; wrong number of list entries)" in {
    val msg = AvatarSearchCriteriaMessage(PlanetSideGUID(1222), List(0))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }

  "encode (failure; list number too big)" in {
    val msg = AvatarSearchCriteriaMessage(PlanetSideGUID(1222), List(0, 0, 0, 0, 0, 256))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }

  "encode (failure; list number too small)" in {
    val msg = AvatarSearchCriteriaMessage(PlanetSideGUID(1222), List(0, 0, 0, -1, 0, 0))
    PacketCoding.encodePacket(msg).isSuccessful mustEqual false
  }
}
