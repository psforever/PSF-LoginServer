// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class FireHintMessageTest extends Specification {
  val string  = hex"a1 0117 23cd63f1d7480d 000077ff9d1d00"
  val string2 = hex"a1 080e 65af5705074411 0000cffee0fc7b08899f5580"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case FireHintMessage(guid1, pos, guid2, guid3, guid4, unk1,u2) =>
        guid1 mustEqual PlanetSideGUID(5889)
        pos mustEqual Vector3(3482.2734f,3642.4922f,53.125f)
        guid2 mustEqual 0
        guid3 mustEqual 65399
        guid4 mustEqual 7581
        unk1 mustEqual 0
        u2 mustEqual None
      case _ =>
        ko
    }
  }
  "decode string2" in {
    PacketCoding.DecodePacket(string2).require match {
      case FireHintMessage(guid1, pos, guid2, guid3, guid4, unk1,u2) =>
        guid1 mustEqual PlanetSideGUID(3592)
        pos mustEqual Vector3(2910.789f,3744.875f,69.0625f)
        guid2 mustEqual 0
        guid3 mustEqual 65231
        guid4 mustEqual 64736
        unk1 mustEqual 3
        u2 mustEqual Some(Vector3(21.5f,-6.8125f,2.65625f))
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = FireHintMessage(PlanetSideGUID(5889), Vector3(3482.2734f,3642.4922f,53.125f), 0, 65399, 7581, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
  "encode string2" in {
    val msg = FireHintMessage(PlanetSideGUID(3592), Vector3(2910.789f,3744.875f,69.0625f), 0, 65231, 64736, 3, Some(Vector3(21.5f,-6.8125f,2.65625f)))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}
