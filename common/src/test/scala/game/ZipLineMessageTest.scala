// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ZipLineMessageTest extends Specification {
  val string = hex"BF 4B00 19 80000010 5bb4089c 52116881 cf76e840"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ZipLineMessage(player_guid, origin_side, action, uid, x, y, z) =>
        player_guid mustEqual PlanetSideGUID(75)
        origin_side mustEqual false
        action mustEqual 0
        uid mustEqual 204
        x mustEqual 1286.9221f
        y mustEqual 1116.5276f
        z mustEqual 91.74034f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ZipLineMessage(PlanetSideGUID(75), false, 0, 204, 1286.9221f, 1116.5276f, 91.74034f)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
