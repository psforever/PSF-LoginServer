// Copyright (c) 2016 PSForever.net to present
package game

import java.net.{InetAddress, InetSocketAddress}

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class VNLWorldStatusMessageTest extends Specification {
  // NOTE: the ServerType is encoded as 0x03 here, but the real planetside server will encode it as 0x04
  val string = hex"0597570065006c0063006f006d006500200074006f00200050006c0061006e00650074005300690064006500210020000186" ++
    hex"67656d696e69" ++ hex"0100 03 00 01459e2540 3775" ++ bin"01".toByteVector

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case VNLWorldStatusMessage(message, worlds) =>
        worlds.length mustEqual 1
        message mustEqual "Welcome to PlanetSide! "
        val world = worlds {
          0
        }
        world.name mustEqual "gemini"
        world.empireNeed mustEqual PlanetSideEmpire.NC
        world.status mustEqual WorldStatus.Up
        world.serverType mustEqual ServerType.Released
        world.connections.length mustEqual 1
        world.connections {
          0
        }.address.getPort mustEqual 30007
        world.connections {
          0
        }.address.getAddress.toString mustEqual "/64.37.158.69"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
      Vector(
        WorldInformation("gemini", WorldStatus.Up, ServerType.Released,
          Vector(
            WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("64.37.158.69"), 30007))
          ), PlanetSideEmpire.NC
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
        WorldInformation("PSForever1", WorldStatus.Up, ServerType.Released, Vector(), PlanetSideEmpire.NC),
        WorldInformation("PSForever2", WorldStatus.Down, ServerType.Beta, Vector(), PlanetSideEmpire.TR)
      ))

    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    //println(pkt)

    // TODO: actually test something
    ok
  }
}
