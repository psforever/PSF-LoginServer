// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.MeritCommendation
import scodec.bits._

class AvatarAwardMessageTest extends Specification {
  val string0 = hex"cf 15010000014000003d0040000000"
  val string1 = hex"cf 2a010000c717b12a0000"
  val string2 = hex"cf a6010000e9058cab0080"
  val string3 = hex"cf 7a010000400000000000"

  "decode (0)" in {
    PacketCoding.decodePacket(string0).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual MeritCommendation.Max1
        unk2 mustEqual AwardProgress(5, 500)
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual MeritCommendation.OneYearVS
        unk2 mustEqual AwardCompletion(1415720846L)
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual MeritCommendation.TwoYearVS
        unk2 mustEqual AwardCompletion(1444482002L)
        unk3 mustEqual 1
      case _ =>
        ko
    }
  }

  "decode (3)" in {
    PacketCoding.decodePacket(string3).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual MeritCommendation.StandardAssault3
        unk2 mustEqual AwardQualificationProgress(0)
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode (0)" in {
    val msg = AvatarAwardMessage(MeritCommendation.Max1, AwardProgress(5, 500))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string0
  }

  "encode (1)" in {
    val msg = AvatarAwardMessage(MeritCommendation.OneYearVS, AwardCompletion(1415720846L))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = AvatarAwardMessage(MeritCommendation.TwoYearVS, AwardCompletion(1444482002L), 1)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }

  "encode (3)" in {
    val msg = AvatarAwardMessage(MeritCommendation.StandardAssault3, AwardQualificationProgress(0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string3
  }
}
