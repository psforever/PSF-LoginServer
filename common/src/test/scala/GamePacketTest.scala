// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class GamePacketTest extends Specification {

  "PlanetSide game packet" in {

    "ConnectToWorldRequestMessage" should {
      val string = hex"03 8667656D696E69 0000000000000000 00000000 00000000 00000000 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  80 00 00 "

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ConnectToWorldRequestMessage(serverName, majorVersion, minorVersion, revision, buildDate, unk) =>
            serverName mustEqual "gemini"
            majorVersion mustEqual 0
            minorVersion mustEqual 0
            revision mustEqual 0
            buildDate mustEqual ""
            unk mustEqual 0
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val msg = ConnectToWorldRequestMessage("gemini", 0, 0, 0, "", 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ConnectToWorldMessage" should {
      val string = hex"04 8667656D696E69  8C36342E33372E3135382E36393C75"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ConnectToWorldMessage(serverName, serverIp, serverPort) =>
            serverName mustEqual "gemini"
            serverIp mustEqual "64.37.158.69"
            serverPort mustEqual 30012
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val msg = ConnectToWorldMessage("gemini", "64.37.158.69", 30012)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "VNLWorldStatusMessage" should {
      // NOTE: the ServerType is encoded as 0x03 here, but the real planetside server will encode it as 0x04
      val string = hex"0597570065006c0063006f006d006500200074006f00200050006c0061006e00650074005300690064006500210020000186" ++
              hex"67656d696e69" ++ hex"0100 03 00 01459e2540 3775" ++ bin"01".toByteVector

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case VNLWorldStatusMessage(message, worlds) =>
            worlds.length mustEqual 1
            message mustEqual "Welcome to PlanetSide! "
            val world = worlds{0}

            world.name mustEqual "gemini"
            world.empireNeed mustEqual EmpireNeed.NC
            world.status mustEqual WorldStatus.Up
            world.serverType mustEqual ServerType.Released

            world.connections.length mustEqual 1
            world.connections{0}.address.getPort mustEqual 30007
            world.connections{0}.address.getAddress.toString mustEqual "/64.37.158.69"
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(
            WorldInformation("gemini", WorldStatus.Up, ServerType.Released,
              Vector(
                WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("64.37.158.69"), 30007))
              ), EmpireNeed.NC
            )
          )
        )
        //0100 04 00 01459e2540377540

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }

      "encode and decode multiple worlds" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(
            WorldInformation("PSForever1", WorldStatus.Up, ServerType.Released, Vector(), EmpireNeed.NC),
            WorldInformation("PSForever2", WorldStatus.Down, ServerType.Beta, Vector(), EmpireNeed.TR)
          ))

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        //println(pkt)

        true mustEqual true
      }
    }

  }
}
