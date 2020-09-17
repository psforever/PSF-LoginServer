// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class RequestDestroyMessageTest extends Specification {
  val string = hex"2D A49C"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case RequestDestroyMessage(object_guid) =>
        object_guid mustEqual PlanetSideGUID(40100)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = RequestDestroyMessage(PlanetSideGUID(40100))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
