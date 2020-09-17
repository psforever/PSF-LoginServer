// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class ProjectileStateMessageTest extends Specification {
  val string = hex"3f 259d c5019 30e4a 9514 c52c9541 d9ba05c2 c5973941 00 f8 ec 02000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case ProjectileStateMessage(projectile, pos, vel, orient, sequence, explode, unk) =>
        projectile mustEqual PlanetSideGUID(40229)
        pos mustEqual Vector3(4611.539f, 5576.375f, 82.328125f)
        vel mustEqual Vector3(18.64686f, -33.43247f, 11.599553f)
        orient mustEqual Vector3(0, 22.5f, 146.25f)
        sequence mustEqual 2
        explode mustEqual false
        unk mustEqual PlanetSideGUID(0)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ProjectileStateMessage(
      PlanetSideGUID(40229),
      Vector3(4611.539f, 5576.375f, 82.328125f),
      Vector3(18.64686f, -33.43247f, 11.599553f),
      Vector3(0, 22.5f, 146.25f),
      2,
      false,
      PlanetSideGUID(0)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    //pkt mustEqual string
    val pkt_bits = pkt.toBitVector
    val str_bits = string.toBitVector
    pkt_bits.take(184) mustEqual str_bits.take(184)                 //skip 1 bit
    pkt_bits.drop(185).take(7) mustEqual str_bits.drop(185).take(7) //skip 1 bit
    pkt_bits.drop(193) mustEqual str_bits.drop(193)
  }
}
