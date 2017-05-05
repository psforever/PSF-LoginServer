// Copyright (c) 2017 PSForever
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
      case PlayerStateMessageUpstream(avatar_guid, pos, vel, facingYaw, facingPitch, facingYawUpper, seq_time, unk1, is_crouching, is_jumping, jump_thrust, is_cloaked, unk2, unk3) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        pos mustEqual Vector3(3694.1094f, 2735.4531f, 90.84375f)
        vel mustEqual Some(Vector3(4.375f, 2.59375f, 0.0f))
        facingYaw mustEqual 10
        facingPitch mustEqual 3
        facingYawUpper mustEqual 0
        seq_time mustEqual 136
        unk1 mustEqual 0
        is_crouching mustEqual false
        is_jumping mustEqual false
        jump_thrust mustEqual false
        is_cloaked mustEqual false
        unk2 mustEqual 112
        unk3 mustEqual 0
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
