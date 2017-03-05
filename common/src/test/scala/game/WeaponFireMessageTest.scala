// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class WeaponFireMessageTest extends Specification {
  val string = hex"34 44130029272F0B5DFD4D4EC5C00009BEF78172003FC0"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case WeaponFireMessage(seq_time, weapon_guid, projectile_guid, shot_origin, unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
        seq_time mustEqual 68
        weapon_guid mustEqual PlanetSideGUID(76)
        projectile_guid mustEqual PlanetSideGUID(40100)
        shot_origin mustEqual Vector3(3675.4688f, 2726.9922f, 92.921875f)
        unk1 mustEqual 0
        unk2 mustEqual 64294
        unk3 mustEqual 1502
        unk4 mustEqual 200
        unk5 mustEqual 255
        unk6 mustEqual 0
        unk7 mustEqual None
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = WeaponFireMessage(68, PlanetSideGUID(76), PlanetSideGUID(40100), Vector3(3675.4688f, 2726.9922f, 92.921875f), 0, 64294, 1502, 200, 255, 0, None)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
