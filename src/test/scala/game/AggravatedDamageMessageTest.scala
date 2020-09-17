// Copyright (c) 2020 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._
import scodec.bits._

class AggravatedDamageMessageTest extends Specification {
  val string = hex"6a350a0e000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case AggravatedDamageMessage(guid, unk) =>
        guid mustEqual PlanetSideGUID(2613)
        unk mustEqual 14
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AggravatedDamageMessage(PlanetSideGUID(2613), 14)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
