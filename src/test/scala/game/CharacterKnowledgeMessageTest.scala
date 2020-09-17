// Copyright (c) 2017 PSForever
package game

import net.psforever.objects.avatar.Certification
import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class CharacterKnowledgeMessageTest extends Specification {
  val string = hex"ec cc637a02 45804600720061006e006b0065006e00740061006e006b0003c022dc0008f01800"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case CharacterKnowledgeMessage(char_id, Some(info)) =>
        char_id mustEqual 41575372L
        info mustEqual CharacterKnowledgeInfo(
          "Frankentank",
          Set(
            Certification.StandardAssault,
            Certification.ArmoredAssault1,
            Certification.MediumAssault,
            Certification.ReinforcedExoSuit,
            Certification.Harasser,
            Certification.Engineering,
            Certification.GroundSupport,
            Certification.AgileExoSuit,
            Certification.AIMAX,
            Certification.StandardExoSuit,
            Certification.AAMAX,
            Certification.ArmoredAssault2
          ),
          15,
          0,
          PlanetSideGUID(12)
        )
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = CharacterKnowledgeMessage(
      41575372L,
      CharacterKnowledgeInfo(
        "Frankentank",
        Set(
          Certification.StandardAssault,
          Certification.ArmoredAssault1,
          Certification.MediumAssault,
          Certification.ReinforcedExoSuit,
          Certification.Harasser,
          Certification.Engineering,
          Certification.GroundSupport,
          Certification.AgileExoSuit,
          Certification.AIMAX,
          Certification.StandardExoSuit,
          Certification.AAMAX,
          Certification.ArmoredAssault2
        ),
        15,
        0,
        PlanetSideGUID(12)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
