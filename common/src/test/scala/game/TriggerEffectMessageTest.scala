// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class TriggerEffectMessageTest extends Specification {
  val string_motionalarmsensor = hex"51 970B 82 6F6E FA00C00000"
  val string_boomer = hex"51 0000 93 737061776E5F6F626A6563745F656666656374 417BB2CB3B4F8E00000000"

  "decode (motion alarm sensor)" in {
    PacketCoding.DecodePacket(string_motionalarmsensor).require match {
      case TriggerEffectMessage(guid, effect, unk, location) =>
        guid mustEqual PlanetSideGUID(2967)
        effect mustEqual "on"
        unk.isDefined mustEqual true
        unk.get.unk1 mustEqual true
        unk.get.unk2 mustEqual 1000L
        location.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (boomer)" in {
    PacketCoding.DecodePacket(string_boomer).require match {
      case TriggerEffectMessage(guid, effect, unk, location) =>
        guid mustEqual PlanetSideGUID(0)
        effect mustEqual "spawn_object_effect"
        unk.isDefined mustEqual false
        location.isDefined mustEqual true
        location.get.pos.x mustEqual 3567.0156f
        location.get.pos.y mustEqual 3278.6953f
        location.get.pos.z mustEqual 114.484375f
        location.get.roll mustEqual 0
        location.get.pitch mustEqual 0
        location.get.yaw mustEqual 0
      case _ =>
        ko
    }
  }

  "encode (motion alarm sensor)" in {
    val msg = TriggerEffectMessage(
      PlanetSideGUID(2967),
      "on",
      Some(TriggeredEffect(true, 1000L)),
      None
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_motionalarmsensor
  }

  "encode (boomer)" in {
    val msg = TriggerEffectMessage(
      PlanetSideGUID(0),
      "spawn_object_effect",
      None,
      Some(TriggeredEffectLocation(
        Vector3(3567.0156f, 3278.6953f, 114.484375f),
        0, 0, 0
      ))
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer
  }
}

