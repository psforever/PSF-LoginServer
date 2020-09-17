// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class GenericObjectActionMessageTest extends Specification {
  val string = hex"56 B501 24"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case GenericObjectActionMessage(object_guid, action) =>
        object_guid mustEqual PlanetSideGUID(437)
        action mustEqual 9
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericObjectActionMessage(PlanetSideGUID(437), 9)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
