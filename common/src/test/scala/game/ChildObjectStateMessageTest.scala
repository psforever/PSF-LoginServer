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
        pitch mustEqual 6
        yaw mustEqual 71
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChildObjectStateMessage(PlanetSideGUID(2916), 6, 71)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
