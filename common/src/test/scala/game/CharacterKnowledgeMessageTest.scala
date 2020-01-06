// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{CertificationType, PlanetSideGUID}
import scodec.bits._

class CharacterKnowledgeMessageTest extends Specification {
  val string = hex"ec cc637a02 45804600720061006e006b0065006e00740061006e006b0003c022dc0008f01800"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case CharacterKnowledgeMessage(char_id, Some(info)) =>
        char_id mustEqual 41575372L
        info mustEqual CharacterKnowledgeInfo(
          "Frankentank",
          Set(
            CertificationType.StandardAssault,
            CertificationType.ArmoredAssault1,
            CertificationType.MediumAssault,
            CertificationType.ReinforcedExoSuit,
            CertificationType.Harasser,
            CertificationType.Engineering,
            CertificationType.GroundSupport,
            CertificationType.AgileExoSuit,
            CertificationType.AIMAX,
            CertificationType.StandardExoSuit,
            CertificationType.AAMAX,
            CertificationType.ArmoredAssault2
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
          CertificationType.StandardAssault,
          CertificationType.ArmoredAssault1,
          CertificationType.MediumAssault,
          CertificationType.ReinforcedExoSuit,
          CertificationType.Harasser,
          CertificationType.Engineering,
          CertificationType.GroundSupport,
          CertificationType.AgileExoSuit,
          CertificationType.AIMAX,
          CertificationType.StandardExoSuit,
          CertificationType.AAMAX,
          CertificationType.ArmoredAssault2
        ),
        15,
        0,
        PlanetSideGUID(12)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
