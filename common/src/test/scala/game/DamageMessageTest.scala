// Copyright (c) 2019 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DamageMessageTest extends Specification {
  val string = hex"0b610b02610b00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DamageMessage(guid1, unk1, guid2, unk2) =>
        guid1 mustEqual PlanetSideGUID(2913)
        unk1 mustEqual 2
        guid2 mustEqual PlanetSideGUID(2913)
        unk2 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DamageMessage(PlanetSideGUID(2913), 2, PlanetSideGUID(2913), false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
