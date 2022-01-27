// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class AvatarAwardMessageTest extends Specification {
  val string0 = hex"cf 15010000014000003d0040000000"
  val string1 = hex"cf 2a010000c717b12a0000"
  val string2 = hex"cf a6010000e9058cab0080"

  "decode (0)" in {
    PacketCoding.decodePacket(string0).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual 277
        unk2 mustEqual AwardOptionZero(5, 500)
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (1)" in {
    PacketCoding.decodePacket(string1).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual 298
        unk2 mustEqual AwardOptionTwo(2831441436L)
        unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.decodePacket(string2).require match {
      case AvatarAwardMessage(unk1, unk2, unk3) =>
        unk1 mustEqual 422
        unk2 mustEqual AwardOptionTwo(2888963748L)
        unk3 mustEqual 2
      case _ =>
        ko
    }
  }

  "encode (0)" in {
    val msg = AvatarAwardMessage(277, AwardOptionZero(5, 500), 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string0
  }

  "encode (1)" in {
    val msg = AvatarAwardMessage(298, AwardOptionTwo(2831441436L), 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = AvatarAwardMessage(422, AwardOptionTwo(2888963748L), 2)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string2
  }
}
