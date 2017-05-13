// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import scodec.bits._

class CharacterCreateRequestMessageTest extends Specification {
  val string = hex"2f 88 54006500730074004300680061007200 320590"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case CharacterCreateRequestMessage(name, head, voice, gender, faction) =>
        name mustEqual "TestChar"
        head mustEqual 50
        voice mustEqual 5
        gender mustEqual CharacterGender.Female
        faction mustEqual PlanetSideEmpire.NC
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = CharacterCreateRequestMessage("TestChar", 50, 5, CharacterGender.Female, PlanetSideEmpire.NC)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

