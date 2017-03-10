// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ChangeFireModeMessageTest extends Specification {
  val string = hex"46 4C0020"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ChangeFireModeMessage(item_guid, fire_mode) =>
        item_guid mustEqual PlanetSideGUID(76)
        fire_mode mustEqual 1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChangeFireModeMessage(PlanetSideGUID(76), 1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
