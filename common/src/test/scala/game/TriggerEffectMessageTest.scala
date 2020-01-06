// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class TriggerEffectMessageTest extends Specification {
  val string_motionalarmsensor = hex"51 970B 82 6F6E FA00C00000"
  val string_boomer = hex"51 0000 93 737061776E5F6F626A6563745F656666656374 417BB2CB3B4F8E00000000"
  val string_boomer_explode = hex"51 DF09 8F 6465746F6E6174655F626F6F6D6572 00"

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
        location.get.pos mustEqual Vector3(3567.0156f, 3278.6953f, 114.484375f)
        location.get.orient mustEqual Vector3(0, 0, 90)
      case _ =>
        ko
    }
  }

  "decode (boomer explode)" in {
    PacketCoding.DecodePacket(string_boomer_explode).require match {
      case TriggerEffectMessage(guid, effect, unk, location) =>
        guid mustEqual PlanetSideGUID(2527)
        effect mustEqual "detonate_boomer"
        unk.isDefined mustEqual false
        location.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (motion alarm sensor)" in {
    val msg = TriggerEffectMessage(PlanetSideGUID(2967), "on", true, 1000L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_motionalarmsensor
  }

  "encode (boomer)" in {
    val msg = TriggerEffectMessage("spawn_object_effect", Vector3(3567.0156f, 3278.6953f, 114.484375f), Vector3(0, 0, 90))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer
  }

  "encode (boomer explode)" in {
    val msg = TriggerEffectMessage(PlanetSideGUID(2527), "detonate_boomer")
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer_explode
  }
}

