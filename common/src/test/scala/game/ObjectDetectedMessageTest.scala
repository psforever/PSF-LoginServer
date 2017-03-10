// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ObjectDetectedMessageTest extends Specification {
  val string = hex"61 E60F E60F 00 1C9C39F8304030AC18A8183436D42C"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ObjectDetectedMessage(guid1, guid2, unk1, unk2) =>
        guid1 mustEqual PlanetSideGUID(4070)
        guid2 mustEqual PlanetSideGUID(4070)
        unk1 mustEqual 0
        unk2.size mustEqual 7
        unk2.head mustEqual 3623
        unk2(1) mustEqual 3198
        unk2(2) mustEqual 3088
        unk2(3) mustEqual 1579
        unk2(4) mustEqual 1578
        unk2(5) mustEqual 3341
        unk2(6) mustEqual 2997
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ObjectDetectedMessage(PlanetSideGUID(4070), PlanetSideGUID(4070), 0, 3623 :: 3198 :: 3088 :: 1579 :: 1578 :: 3341 :: 2997 :: Nil)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
