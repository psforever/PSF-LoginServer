// Copyright (c) 2017 PSForever
import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control.{ClientStart, ServerStart}
import scodec.bits._

class PacketCodingTest extends Specification {
  /*def roundTrip[Container <: PlanetSidePacketContainer, Packet <: PlanetSidePacket](cont : Container, pkt : Packet) = {

    val filledContainer = cont match {
      case x : ControlPacket => x.copy(packet = pkt.asInstanceOf[PlanetSideControlPacket])
    }
    val pktEncoded = PacketCoding.MarshalPacket(ControlPacket(packetUnderTest.opcode, packetUnderTest)).require
    val pktDecoded = PacketCoding.UnMarshalPacket(pkt.toByteVector).require.asInstanceOf[ControlPacket]
    val recvPkt = decoded.packet.asInstanceOf[ServerStart]

  }*/

  "Packet coding" should {
    "correctly decode control packets" in {
      val packet = PacketCoding.UnmarshalPacket(hex"0001 00000002 00261e27 000001f0").require

      packet.isInstanceOf[ControlPacket] mustEqual true

      val controlPacket = packet.asInstanceOf[ControlPacket]
      controlPacket.opcode mustEqual ControlPacketOpcode.ClientStart
      controlPacket.packet mustEqual ClientStart(656287232)
    }

    "encode and decode to identical packets" in {
      val clientNonce = 213129
      val serverNonce = 848483

      val packetUnderTest = ServerStart(clientNonce, serverNonce)
      val pkt = PacketCoding.MarshalPacket(ControlPacket(packetUnderTest.opcode, packetUnderTest)).require

      val decoded = PacketCoding.UnmarshalPacket(pkt.toByteVector).require.asInstanceOf[ControlPacket]
      val recvPkt = decoded.packet.asInstanceOf[ServerStart]

      packetUnderTest mustEqual recvPkt
    }

    "reject corrupted control packets" in {
      val packet = PacketCoding.UnmarshalPacket(hex"0001 00001002 00261e27 004101f0")

      packet.isSuccessful mustEqual false
    }

    "correctly decode crypto packets" in {
      val packet = PacketCoding.UnmarshalPacket(hex"0001 00000002 00261e27 000001f0").require

      packet.isInstanceOf[ControlPacket] mustEqual true

      val controlPacket = packet.asInstanceOf[ControlPacket]
      controlPacket.opcode mustEqual ControlPacketOpcode.ClientStart
      controlPacket.packet mustEqual ClientStart(656287232)
    }

    "reject bad packet types" in {
      PacketCoding.UnmarshalPacket(hex"ff414141").isFailure mustEqual true
    }

    "reject small packets" in {
      PacketCoding.UnmarshalPacket(hex"00").isFailure mustEqual true
      PacketCoding.UnmarshalPacket(hex"").isFailure mustEqual true
    }
  }

}
