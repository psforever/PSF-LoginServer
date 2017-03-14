// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class ProjectileStateMessageTest extends Specification {
  val string = hex"3f 259d c5019 30e4a 9514 c52c9541 d9ba05c2 c5973941 00 f8 ec 020000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ProjectileStateMessage(projectile, pos, aim, unk1, unk2, unk3, unk4, unk5) =>
        projectile mustEqual PlanetSideGUID(40229)
        pos.x mustEqual 4611.539f
        pos.y mustEqual 5576.375f
        pos.z mustEqual 82.328125f
        aim.x mustEqual 18.64686f
        aim.y mustEqual -33.43247f
        aim.z mustEqual 11.599553f
        unk1 mustEqual 0
        unk2 mustEqual 248
        unk3 mustEqual 236
        unk4 mustEqual false
        unk5 mustEqual 4
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ProjectileStateMessage(
      PlanetSideGUID(40229),
      Vector3(4611.539f, 5576.375f, 82.328125f),
      Vector3(18.64686f, -33.43247f, 11.599553f),
      0, 248, 236, false, 4
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
