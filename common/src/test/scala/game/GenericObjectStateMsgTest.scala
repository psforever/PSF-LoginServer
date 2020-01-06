// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class GenericObjectStateMsgTest extends Specification {
  val string = hex"1D 6401 10000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case GenericObjectStateMsg(object_guid, state) =>
        object_guid mustEqual PlanetSideGUID(356)
        state mustEqual 16
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = GenericObjectStateMsg(PlanetSideGUID(356), 16)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
