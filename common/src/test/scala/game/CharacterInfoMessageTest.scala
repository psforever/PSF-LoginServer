// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class CharacterInfoMessageTest extends Specification {
  val string = hex"14 0F000000 10270000C1D87A024B00265CB08000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case CharacterInfoMessage(unk, zone, charId, guid, finished, last) =>
        unk mustEqual 15L
        zone mustEqual PlanetSideZoneID(10000)
        charId mustEqual 41605313L
        guid mustEqual PlanetSideGUID(75)
        finished mustEqual false
        last mustEqual 6404428L
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = CharacterInfoMessage(15L, PlanetSideZoneID(10000), 41605313L, PlanetSideGUID(75), false, 6404428L)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
