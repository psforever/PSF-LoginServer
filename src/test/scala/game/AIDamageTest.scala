// Copyright (c) 2023 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class AIDamageTest extends Specification {
  val string1 = hex"5c de10 89e8 38030000 00000000 04020000"

  "decode" in {
    PacketCoding.decodePacket(string1).require match {
      case AIDamage(target_guid, attacker_guid, projectile_type, unk1, unk2) =>
        target_guid mustEqual PlanetSideGUID(4318)
        attacker_guid mustEqual PlanetSideGUID(59529)
        projectile_type mustEqual 824L
        unk1 mustEqual 0L
        unk2 mustEqual 516L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AIDamage(PlanetSideGUID(4318), PlanetSideGUID(59529), 824L, 0L, 516L)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }
}
