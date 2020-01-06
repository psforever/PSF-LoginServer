// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class UnuseItemMessageTest extends Specification {
  val string = hex"26 4B00 340D"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case UnuseItemMessage(player, item) =>
        player mustEqual PlanetSideGUID(75)
        item mustEqual PlanetSideGUID(3380)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = UnuseItemMessage(PlanetSideGUID(75), PlanetSideGUID(3380))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
