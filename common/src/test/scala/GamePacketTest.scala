// Copyright (c) 2016 PSForever.net to present
import org.specs2.mutable._
import psforever.net._
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
            worlds{0}.name mustEqual "gemini"
            worlds{0}.empireNeed mustEqual EmpireNeed.NC
            worlds{0}.status mustEqual WorldStatus.Up
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(WorldInformation("gemini", WorldStatus.Up, ServerType.Beta, EmpireNeed.NC)))
        //0100 04 00 01459e2540377540

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }

      "encode and decode multiple worlds" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(
            WorldInformation("PSForever1", WorldStatus.Up, ServerType.Released, EmpireNeed.NC),
            WorldInformation("PSForever2", WorldStatus.Down, ServerType.Beta, EmpireNeed.TR)
          ))

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        println(pkt)

        true mustEqual true
      }
    }

  }
}
