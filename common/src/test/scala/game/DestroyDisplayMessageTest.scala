// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideEmpire
import scodec.bits._

class DestroyDisplayMessageTest extends Specification {
  val string = hex"81 87 41006E00670065006C006C006F00 35BCD801 8 F201 9207 0A 0 48004D00460049004300 B18ED901 00" // Angello-VS (???) HMFIC-TR

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DestroyDisplayMessage(killer, killer_charId, killer_empire, killer_inVehicle, unk, method, victim, victim_charId, victim_empire, victim_inVehicle) =>
        killer mustEqual "Angello"
        killer_charId mustEqual 30981173
        killer_empire mustEqual PlanetSideEmpire.VS
        killer_inVehicle mustEqual false
        unk mustEqual 121
        method mustEqual 969
        victim mustEqual "HMFIC"
        victim_charId mustEqual 31035057
        victim_empire mustEqual PlanetSideEmpire.TR
        victim_inVehicle mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DestroyDisplayMessage("Angello", 30981173, PlanetSideEmpire.VS, false, 121, 969, "HMFIC", 31035057, PlanetSideEmpire.TR, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
