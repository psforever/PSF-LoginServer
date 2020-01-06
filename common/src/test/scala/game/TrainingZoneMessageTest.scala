// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class TrainingZoneMessageTest extends Specification {
  val string = hex"75 1300 0000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case TrainingZoneMessage(zone, _) =>
        zone mustEqual PlanetSideGUID(19)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TrainingZoneMessage(PlanetSideGUID(19))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
