// Copyright (c) 2017 PSForever
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
        message mustEqual "Welcome to PlanetSide! "

        worlds.length mustEqual 1

        val world = worlds(0)
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
      Vector(WorldInformation("gemini", WorldStatus.Up, ServerType.Released,
        Vector(
          WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("64.37.158.69"), 30007))
        ), PlanetSideEmpire.NC
      ))
    )

    //0100 04 00 01459e2540377540

    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode and decode empty messages" in {
    val string = hex"0584410041004100410000"
    val empty_msg = VNLWorldStatusMessage("AAAA", Vector())
    val empty_pkt = PacketCoding.EncodePacket(empty_msg).require.toByteVector

    empty_pkt mustEqual string

    PacketCoding.DecodePacket(string).require match {
      case VNLWorldStatusMessage(message, worlds) =>
        message mustEqual "AAAA"
        worlds.length mustEqual 0
      case _ =>
        ko
    }
  }

  "encode and decode multiple worlds" in {
    var string = hex"0597570065006c0063006f006d006500200074006f00200050006c0061006e0065007400530069006400650021002000048941424344414243443101000300006240414243444142434432000002020022404142434441424344330000010100a2404142434441424344340500040000c0"

    val worlds = Vector(
        WorldInformation("ABCDABCD1", WorldStatus.Up, ServerType.Released, Vector(), PlanetSideEmpire.NC),
        WorldInformation("ABCDABCD2", WorldStatus.Down, ServerType.Beta, Vector(), PlanetSideEmpire.TR),
        WorldInformation("ABCDABCD3", WorldStatus.Locked, ServerType.Development, Vector(), PlanetSideEmpire.VS),
        WorldInformation("ABCDABCD4", WorldStatus.Full, ServerType.Released_Gemini, Vector(), PlanetSideEmpire.NEUTRAL)
    )

    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ", worlds) 
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string

    PacketCoding.DecodePacket(string).require match {
      case VNLWorldStatusMessage(message, pkt_worlds) =>
        message mustEqual "Welcome to PlanetSide! "

        pkt_worlds.length mustEqual worlds.length

        for (i <- 0 to pkt_worlds.length-1)
          pkt_worlds(i) mustEqual worlds(i)

        ok
      case _ =>
        ko
    }
  }
}
