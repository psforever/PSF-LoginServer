// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGeneratorState}
import scodec.bits._

class BuildingInfoUpdateMessageTest extends Specification {
  val string = hex"a0 04 00 09 00 16 00 00 00 00 80 00 00 00 17 00  00 00 00 00 00 40"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case BuildingInfoUpdateMessage(continent_guid,
      building_guid,
      ntu_level,
      is_hacked,
      empire_hack,
      hack_time_remaining,
      empire_own,
      unk1,
      unk1x,
      generator_state,
      spawn_tubes_normal,
      force_dome_active,
      lattice_benefit,
      unk3,
      unk4,
      unk5,
      unk6,
      unk7,
      unk7x,
      boost_spawn_pain,
      boost_generator_pain) =>
        continent_guid mustEqual 4
        building_guid mustEqual 9
        ntu_level mustEqual 1
        is_hacked mustEqual false
        empire_hack mustEqual PlanetSideEmpire.NEUTRAL
        hack_time_remaining mustEqual 0
        empire_own mustEqual PlanetSideEmpire.NC
        unk1 mustEqual 0
        unk1x mustEqual None
        generator_state mustEqual PlanetSideGeneratorState.Normal
        spawn_tubes_normal mustEqual true
        force_dome_active mustEqual false
        lattice_benefit mustEqual 28
        unk3 mustEqual 0
        unk4.size mustEqual 0
        unk4.isEmpty mustEqual true
        unk5 mustEqual 0
        unk6 mustEqual false
        unk7 mustEqual 8
        unk7x mustEqual None
        boost_spawn_pain mustEqual false
        boost_generator_pain mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = BuildingInfoUpdateMessage(4,
      9,
      1,
      false,
      PlanetSideEmpire.NEUTRAL,
      0,
      PlanetSideEmpire.NC,
      0,
      None,
      PlanetSideGeneratorState.Normal,
      true,
      false,
      28,
      0,
      Nil,
      0,
      false,
      8,
      None,
      false,
      false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
