// Copyright (c) 2020 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class AggravatedDamageMessageTest extends Specification {
  val string = hex"6a350a0e000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AggravatedDamageMessage(guid,unk) =>
        guid mustEqual PlanetSideGUID(2613)
        unk mustEqual 14
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AggravatedDamageMessage(PlanetSideGUID(2613), 14)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
