// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class TriggerEnvironmentalDamageMessageTest extends Specification {
  val string = hex"74 a7c44140000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case TriggerEnvironmentalDamageMessage(unk1, guid, unk2) =>
        unk1 mustEqual 2
        guid mustEqual PlanetSideGUID(4511)
        unk2 mustEqual 5L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TriggerEnvironmentalDamageMessage(2, PlanetSideGUID(4511), 5L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
