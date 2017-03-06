// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{PlayerStateShiftMessage, ShiftState}
import net.psforever.types.Vector3
import scodec.bits._

class PlayerStateShiftMessageTest extends Specification {
  val string_short = hex"BE 68"
  val string_pos = hex"BE 95 A0 89 13 91 B8 B0 BF F0"
  val string_posAndVel = hex"BE AE 01 29 CD 59 B9 40 C0 EA D4 00 0F 86 40"

  "decode (short)" in {
    PacketCoding.DecodePacket(string_short).require match {
      case PlayerStateShiftMessage(state, unk) =>
        state.isDefined mustEqual false
        unk.isDefined mustEqual true
        unk.get mustEqual 5
      case _ =>
        ko
    }
  }

  "decode (pos)" in {
    PacketCoding.DecodePacket(string_pos).require match {
      case PlayerStateShiftMessage(state, unk) =>
        state.isDefined mustEqual true
        state.get.unk mustEqual 1
        state.get.pos.x mustEqual 4624.703f
        state.get.pos.y mustEqual 5922.1484f
        state.get.pos.z mustEqual 46.171875f
        state.get.viewYawLim mustEqual 255
        state.get.vel.isDefined mustEqual false
        unk.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (pos and vel)" in {
    PacketCoding.DecodePacket(string_posAndVel).require match {
      case PlayerStateShiftMessage(state, unk) =>
        state.isDefined mustEqual true
        state.get.unk mustEqual 2
        state.get.pos.x mustEqual 4645.75f
        state.get.pos.y mustEqual 5811.6016f
        state.get.pos.z mustEqual 50.3125f
        state.get.viewYawLim mustEqual 14
        state.get.vel.isDefined mustEqual true
        state.get.vel.get.x mustEqual 2.8125f
        state.get.vel.get.y mustEqual -8.0f
        state.get.vel.get.z mustEqual 0.375f
        unk.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (short)" in {
    val msg = PlayerStateShiftMessage(5)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_short
  }

  "encode (pos)" in {
    val msg = PlayerStateShiftMessage(ShiftState(1, Vector3(4624.703f, 5922.1484f, 46.171875f), 255))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_pos
  }

  "encode (pos and vel)" in {
    val msg = PlayerStateShiftMessage(ShiftState(2, Vector3(4645.75f, 5811.6016f, 50.3125f), 14, Vector3(2.8125f, -8.0f, 0.375f)))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_posAndVel
  }
}
