// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class PlayerStateMessageUpstreamTest extends Specification {
  val string = hex"BD 4B000 E377BA575B616C640A70004014060110007000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case PlayerStateMessageUpstream(avatar_guid, pos, vel, unk1, aim_pitch, unk2, seq_time, unk3, is_crouching, unk4, unk5, is_cloaking, unk6, unk7) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        pos mustEqual Vector3(3694.1094f, 2735.4531f, 90.84375f)
        vel mustEqual Some(Vector3(4.375f, 2.59375f, 0.0f))
        unk1 mustEqual 10
        aim_pitch mustEqual 3
        unk2 mustEqual 0
        seq_time mustEqual 136
        unk3 mustEqual 0
        is_crouching mustEqual false
        unk4 mustEqual false
        unk5 mustEqual false
        is_cloaking mustEqual false
        unk6 mustEqual 112
        unk7 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PlayerStateMessageUpstream(PlanetSideGUID(75), Vector3(3694.1094f, 2735.4531f, 90.84375f), Some(Vector3(4.375f, 2.59375f, 0.0f)), 10, 3, 0, 136, 0, false, false, false, false, 112, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
