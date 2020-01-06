// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class SquadStateTest extends Specification {
  val string1 = hex"770700186d9130081001b11b27c1c041680000"
  val string2 = hex"770700242a28c020003e9237a90e3382695004eab58a0281017eb95613df4c42950040"
  val stringx = hex"7704008dd9ccf010042a9837310e1b82a8c006646c7a028103984f34759c904a800014f01c26f3d014081ddd3896931bc25478037680ea80c081d699a147b01e154000031c0bc81407e08c1a3a890de1542c022070bd0140815958bf29efa6214300108023c01000ae491ac68d1a61342c023623c50140011d6ea0878f3026a00009e014"

  "decode (1)" in {
    PacketCoding.DecodePacket(string1).require match {
      case SquadState(guid, list) =>
        guid mustEqual PlanetSideGUID(7)
        list.size mustEqual 1
        //0
        list.head match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 1300870L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(3464.0469f, 4065.5703f, 20.015625f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 0
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.DecodePacket(string2).require match {
      case SquadState(guid, list) =>
        guid mustEqual PlanetSideGUID(7)
        list.size mustEqual 2
        //0
        list.head match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 42771010L
            u2 mustEqual 0
            u3 mustEqual 0
            pos mustEqual Vector3(6801.953f, 4231.828f, 39.21875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 680
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(1) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 42644970L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(2908.7422f, 3742.6875f, 67.296875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 680
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "decode (8)" in {
    PacketCoding.DecodePacket(stringx).require match {
      case SquadState(guid, list) =>
        guid mustEqual PlanetSideGUID(4)
        list.size mustEqual 8
        //0
        list.head match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 30383325L
            u2 mustEqual 0
            u3 mustEqual 16
            pos mustEqual Vector3(6849.328f, 4231.5938f, 41.71875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 864
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(1) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 41577572L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(6183.797f, 4013.6328f, 72.5625f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 0
            u8.contains(335) mustEqual true
            u9.contains(true) mustEqual true
          case _ =>
            ko
        }
        list(2) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 41606788L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(6611.8594f, 4242.586f, 75.46875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 888
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(3) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 30736877L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(6809.836f, 4218.078f, 40.234375f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 0
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(4) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 41517411L
            u2 mustEqual 64
            u3 mustEqual 63
            pos mustEqual Vector3(6848.0312f, 4232.2266f, 41.734375f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 556
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(5) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 41607488L
            u2 mustEqual 64
            u3 mustEqual 64
            pos mustEqual Vector3(2905.3438f, 3743.9453f, 67.296875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 304
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(6) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 41419792L
            u2 mustEqual 0
            u3 mustEqual 5
            pos mustEqual Vector3(6800.8906f ,4236.7734f, 39.296875f)
            u4 mustEqual 2
            u5 mustEqual 2
            u6 mustEqual false
            u7 mustEqual 556
            u8.isEmpty mustEqual true
            u9.isEmpty mustEqual true
          case _ =>
            ko
        }
        list(7) match {
          case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9) =>
            char_id mustEqual 42616684L
            u2 mustEqual 64
            u3 mustEqual 0
            pos mustEqual Vector3(2927.1094f, 3704.0312f, 78.375f)
            u4 mustEqual 1
            u5 mustEqual 1
            u6 mustEqual false
            u7 mustEqual 0
            u8.contains(572) mustEqual true
            u9.contains(true) mustEqual true
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = SquadState(PlanetSideGUID(7), List(
      SquadStateInfo(1300870L, 64, 64, Vector3(3464.0469f, 4065.5703f, 20.015625f), 2, 2, false, 0)
    ))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string1
  }

  "encode (2)" in {
    val msg = SquadState(PlanetSideGUID(7), List(
      SquadStateInfo(42771010L, 0, 0, Vector3(6801.953f, 4231.828f, 39.21875f), 2, 2, false, 680),
      SquadStateInfo(42644970L, 64, 64, Vector3(2908.7422f, 3742.6875f, 67.296875f), 2, 2, false, 680)
    ))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string2
  }

  "encode (8)" in {
    val msg = SquadState(PlanetSideGUID(4), List(
      SquadStateInfo(30383325L, 0, 16, Vector3(6849.328f, 4231.5938f, 41.71875f), 2, 2, false, 864),
      SquadStateInfo(41577572L, 64, 64, Vector3(6183.797f, 4013.6328f, 72.5625f), 2, 2, false, 0, 335, true),
      SquadStateInfo(41606788L, 64, 64, Vector3(6611.8594f, 4242.586f, 75.46875f), 2, 2, false, 888),
      SquadStateInfo(30736877L, 64, 64, Vector3(6809.836f, 4218.078f, 40.234375f), 2, 2, false, 0),
      SquadStateInfo(41517411L, 64, 63, Vector3(6848.0312f, 4232.2266f, 41.734375f), 2, 2, false, 556),
      SquadStateInfo(41607488L, 64, 64, Vector3(2905.3438f, 3743.9453f, 67.296875f), 2, 2, false, 304),
      SquadStateInfo(41419792L, 0, 5, Vector3(6800.8906f, 4236.7734f, 39.296875f), 2, 2, false, 556),
      SquadStateInfo(42616684L, 64, 0, Vector3(2927.1094f, 3704.0312f, 78.375f), 1, 1, false, 0, 572, true)
    ))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual stringx
  }
}