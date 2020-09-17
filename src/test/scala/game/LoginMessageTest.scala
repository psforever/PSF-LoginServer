// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class LoginMessageTest extends Specification {
  val string_password = hex"0x01030000000f0000008b4465632020322032303039408061816154000000"
  val string_token =
    hex"0x01030000000f0000008b4465632020322032303039a0a0a0a0a121212121a1a1a1a222222222a2a2a2a323232323a3a3a3a424240040806154000000"

  "LoginMessage" should {
    "decode (username)" in {
      PacketCoding.decodePacket(string_password).require match {
        case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
          majorVersion mustEqual 3
          minorVersion mustEqual 15
          buildDate mustEqual "Dec  2 2009"
          username mustEqual "a"
          password mustEqual Some("a")
          token mustEqual None
          revision mustEqual 84
        case _ =>
          ko
      }
    }

    "decode (token)" in {
      PacketCoding.decodePacket(string_token).require match {
        case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
          majorVersion mustEqual 3
          minorVersion mustEqual 15
          buildDate mustEqual "Dec  2 2009"
          username mustEqual "a"
          password mustEqual None
          token mustEqual Some("AAAABBBBCCCCDDDDEEEEFFFFGGGGHHH")
          revision mustEqual 84
        case _ =>
          ko
      }
    }

    "encode (username)" in {
      val msg = LoginMessage(
        3,
        15,
        "Dec  2 2009",
        "a",
        Some("a"),
        None,
        84
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_password
    }
  }

  "encode (token)" in {
    val msg = LoginMessage(
      3,
      15,
      "Dec  2 2009",
      "a",
      None,
      Some("AAAABBBBCCCCDDDDEEEEFFFFGGGGHHH"),
      84
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_token
  }

  "encode (both?)" in {
    LoginMessage(
      3,
      15,
      "Dec  2 2009",
      "a",
      Some("a"),
      Some("AAAABBBBCCCCDDDDEEEEFFFFGGGGHHH"),
      84
    ) must throwA[IllegalArgumentException]
  }

  "encode (majorVersion == -1)" in {
    LoginMessage(
      -1,
      15,
      "Dec  2 2009",
      "a",
      Some("a"),
      None,
      84
    ) must throwA[IllegalArgumentException]
  }

  "encode (minorVersion == -1)" in {
    LoginMessage(
      3,
      -1,
      "Dec  2 2009",
      "a",
      Some("a"),
      None,
      84
    ) must throwA[IllegalArgumentException]
  }

  "encode (revision == -1)" in {
    LoginMessage(
      3,
      15,
      "Dec  2 2009",
      "a",
      Some("a"),
      None,
      -1
    ) must throwA[IllegalArgumentException]
  }
}
