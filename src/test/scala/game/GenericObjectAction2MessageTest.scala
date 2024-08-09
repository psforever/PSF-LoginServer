// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class GenericObjectAction2MessageTest extends Specification {
  val string = hex"80 38C139212 0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case GenericObjectAction2Message(unk, guid1, guid2) =>
        unk mustEqual 1
        guid1 mustEqual PlanetSideGUID(2502)
        guid2 mustEqual PlanetSideGUID(2505)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericObjectAction2Message(1, PlanetSideGUID(2502), PlanetSideGUID(2505))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
