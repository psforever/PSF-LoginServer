// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ChildObjectStateMessageTest extends Specification {
  val string = hex"1E 640B 06 47"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ChildObjectStateMessage(object_guid, pitch, yaw) =>
        object_guid mustEqual PlanetSideGUID(2916)
        pitch mustEqual 343.125f
        yaw mustEqual 160.3125f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChildObjectStateMessage(PlanetSideGUID(2916), 343.125f, 160.3125f)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
