// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class VoiceHostRequestTest extends Specification {
  val string_request_local_75_high      = hex"b0 25 80 01c000"
  val string_request_local_1111_mid     = hex"b0 2b 82 64c000"
  val string_request_local_1112_mid     = hex"b0 2c 02 64c000"
  val string_request_remote_12345_high  = hex"b0 9c 98 01c780 3131312e3232322e3132332e323334"

  "decode local 75 high" in {
    PacketCoding.decodePacket(string_request_local_75_high).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, data) =>
        remote_host mustEqual false
        port mustEqual 75
        bandwidth mustEqual 3
        data mustEqual ""
      case _ =>
        ko
    }
  }

  "encode local 75 high" in {
    val msg = VoiceHostRequest(remote_host = false, 75, 3, "")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_75_high
  }

  "decode local 1111 mid" in {
    PacketCoding.decodePacket(string_request_local_1111_mid).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, data) =>
        remote_host mustEqual false
        port mustEqual 1111
        bandwidth mustEqual 201
        data mustEqual ""
      case _ =>
        ko
    }
  }

  "encode local 1111 mid" in {
    val msg = VoiceHostRequest(remote_host = false, 1111, 201, "")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_1111_mid
  }

  "decode local 1112 mid" in {
    PacketCoding.decodePacket(string_request_local_1112_mid).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, data) =>
        remote_host mustEqual false
        port mustEqual 1112
        bandwidth mustEqual 201
        data mustEqual ""
      case _ =>
        ko
    }
  }

  "encode local 1112 mid" in {
    val msg = VoiceHostRequest(remote_host = false, 1112, 201, "")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_1112_mid
  }

  "decode remote 12345 high" in {
    PacketCoding.decodePacket(string_request_remote_12345_high).require match {
      case VoiceHostRequest(remote, port, codec, server_ip) =>
        remote mustEqual true
        port mustEqual 12345
        codec mustEqual 3
        server_ip mustEqual "111.222.123.234"
      case _ =>
        ko
    }
  }

  "encode remote 12345 high" in {
    val msg = VoiceHostRequest(remote_host = true, port = 12345, bandwidth = 3, remote_ip = "111.222.123.234")
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_remote_12345_high
  }
}
