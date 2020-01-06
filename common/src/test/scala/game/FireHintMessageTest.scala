// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class FireHintMessageTest extends Specification {
  val string  = hex"a1 0117 23cd63f1d7480d 000077ff9d1d00"
  val string2 = hex"a1 080e 65af5705074411 0000cffee0fc7b08899f5580"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case FireHintMessage(weapon_guid, pos, u1, u2, u3, u4, u5) =>
        weapon_guid mustEqual PlanetSideGUID(5889)
        pos mustEqual Vector3(3482.2734f,3642.4922f,53.125f)
        u1 mustEqual 0
        u2 mustEqual 65399
        u3 mustEqual 7581
        u4 mustEqual 0
        u5 mustEqual None
      case _ =>
        ko
    }
  }
  "decode string2" in {
    PacketCoding.DecodePacket(string2).require match {
      case FireHintMessage(weapon_guid, pos, u1, u2, u3, u4, u5) =>
        weapon_guid mustEqual PlanetSideGUID(3592)
        pos mustEqual Vector3(2910.789f,3744.875f,69.0625f)
        u1 mustEqual 0
        u2 mustEqual 65231
        u3 mustEqual 64736
        u4 mustEqual 3
        u5 mustEqual Some(Vector3(21.5f,-6.8125f,2.65625f))
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
