// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types._
import scodec.bits._

class PlayerStateMessageTest extends Specification {
  val string_short = hex"08 A006 DFD17 B5AEB 380B 0F80002990"
  val string_mod = hex"08 A006 DFD17 B5AEB 380B 0F80002985" //slightly modified from above to demonstrate active booleans
  val string_vel = hex"08 A006 4DD47 CDB1B 0C0B A8C1A5000403008014A4"

  "decode (short)" in {
    PacketCoding.DecodePacket(string_short).require match {
      case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, jthrust, cloaked) =>
        guid mustEqual PlanetSideGUID(1696)
        pos.x mustEqual 4003.7422f
        pos.y mustEqual 5981.414f
        pos.z mustEqual 44.875f
        vel.isDefined mustEqual false
        facingYaw mustEqual 2.8125f
        facingPitch mustEqual 0f
        facingUpper mustEqual 0f
        unk1 mustEqual 83
        crouching mustEqual false
        jumping mustEqual false
        jthrust mustEqual false
        cloaked mustEqual false
      case _ =>
        ko
    }
  }

  "decode (mod)" in {
    PacketCoding.DecodePacket(string_mod).require match {
      case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, jthrust, cloaked) =>
        guid mustEqual PlanetSideGUID(1696)
        pos.x mustEqual 4003.7422f
        pos.y mustEqual 5981.414f
        pos.z mustEqual 44.875f
        vel.isDefined mustEqual false
        facingYaw mustEqual 2.8125f
        facingPitch mustEqual 0f
        facingUpper mustEqual 0f
        unk1 mustEqual 83
        crouching mustEqual false
        jumping mustEqual true
        jthrust mustEqual false
        cloaked mustEqual true
      case _ =>
        ko
    }
  }

  "decode (vel)" in {
    PacketCoding.DecodePacket(string_vel).require match {
      case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, jthrust, cloaked) =>
        guid mustEqual PlanetSideGUID(1696)
        pos.x mustEqual 4008.6016f
        pos.y mustEqual 5987.6016f
        pos.z mustEqual 44.1875f
        vel.isDefined mustEqual true
        vel.get.x mustEqual 2.53125f
        vel.get.y mustEqual 6.5625f
        vel.get.z mustEqual 0.0f
        facingYaw mustEqual 22.5f
        facingPitch mustEqual -11.25f
        facingUpper mustEqual 0f
        unk1 mustEqual 165
        crouching mustEqual false
        jumping mustEqual false
        jthrust mustEqual false
        cloaked mustEqual false
      case _ =>
        ko
    }
  }

  "encode (short)" in {
    val msg = PlayerStateMessage(
      PlanetSideGUID(1696),
      Vector3(4003.7422f, 5981.414f, 44.875f),
      None,
      2.8125f, 0f, 0f, 83,
      false, false, false, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string_short
  }

  "encode (mod)" in {
    val msg = PlayerStateMessage(
      PlanetSideGUID(1696),
      Vector3(4003.7422f, 5981.414f, 44.875f),
      None,
      2.8125f, 0f, 0f, 83,
      false, true, false, true)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string_mod
  }

  "encode (vel)" in {
    val msg = PlayerStateMessage(
      PlanetSideGUID(1696),
      Vector3(4008.6016f, 5987.6016f, 44.1875f),
      Some(Vector3(2.53125f, 6.5625f, 0f)),
      22.5f, -11.25f, 0f, 165,
      false, false, false, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string_vel
  }
}
