// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import scodec.bits._

class AvatarDeadStateMessageTest extends Specification {
  val string         = hex"ad3c1260801c12608009f99861fb0741e040000010"
  val string_invalid = hex"ad3c1260801c12608009f99861fb0741e0400000F0"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case AvatarDeadStateMessage(unk1, unk2, unk3, pos, unk4, unk5) =>
        unk1 mustEqual DeadState.Dead
        unk2 mustEqual 300000
        unk3 mustEqual 300000
        pos mustEqual Vector3(6552.617f, 4602.375f, 60.90625f)
        unk4 mustEqual PlanetSideEmpire.VS
        unk5 mustEqual true
      case _ =>
        ko
    }
  }

  "decode (failure)" in {
    PacketCoding.decodePacket(string_invalid).isFailure mustEqual true
  }

  "encode" in {
    val msg = AvatarDeadStateMessage(
      DeadState.Dead,
      300000,
      300000,
      Vector3(6552.617f, 4602.375f, 60.90625f),
      PlanetSideEmpire.VS,
      true
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
