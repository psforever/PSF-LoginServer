// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ChangeFireStateMessage_StopTest extends Specification {
  val string = hex"3A 4C00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ChangeFireStateMessage_Stop(item_guid) =>
        item_guid mustEqual PlanetSideGUID(76)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChangeFireStateMessage_Stop(PlanetSideGUID(76))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
