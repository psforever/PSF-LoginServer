// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class LongRangeProjectileInfoMessageTest extends Specification {
  val string = hex"c7 d214 006c485fd9c307ed30790f84a0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case LongRangeProjectileInfoMessage(guid, pos, vel) =>
        guid mustEqual PlanetSideGUID(5330)
        pos mustEqual Vector3(2264, 5115.039f, 31.046875f)
        vel.contains(Vector3(-57.1875f, 9.875f, 47.5f)) mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = LongRangeProjectileInfoMessage(
      PlanetSideGUID(5330),
      Vector3(2264, 5115.039f, 31.046875f),
      Vector3(-57.1875f, 9.875f, 47.5f)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
