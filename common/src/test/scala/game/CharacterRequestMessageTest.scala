// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import scodec.bits._

class CharacterRequestMessageTest extends Specification {
  val string = hex"30 c1d87a02 00000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case CharacterRequestMessage(charId, action) =>
        charId mustEqual 41605313L
        action mustEqual CharacterRequestAction.Select
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = CharacterRequestMessage(41605313L, CharacterRequestAction.Select)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

