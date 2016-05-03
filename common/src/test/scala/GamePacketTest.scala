// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import org.specs2.mutable._
import net.psforever.packet._
import scodec.bits._

class GamePacketTest extends Specification {

  "PlanetSide game packet" in {
    val cNonce = 656287232

    "VNLWorldStatusMessage" should {
      val string = hex"0597570065006c0063006f006d006500200074006f00200050006c0061006e00650074005300690064006500210020000186" ++
              hex"67656d696e69" ++ hex"0100 01 00 01459e2540 3775" ++ bin"01".toByteVector

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case VNLWorldStatusMessage(message, worlds) =>
            worlds.length mustEqual 1
            message mustEqual "Welcome to PlanetSide! "
            val world = worlds{0}

            world.name mustEqual "gemini"
            world.empireNeed mustEqual EmpireNeed.NC
            world.status mustEqual WorldStatus.Up

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
            WorldInformation("gemini", WorldStatus.Up, ServerType.Beta,
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
