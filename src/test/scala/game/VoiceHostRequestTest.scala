// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class VoiceHostRequestTest extends Specification {
  val string_request_local_75_high      = hex"b0 2580 01 c0 00"
  val string_request_local_1111_mid     = hex"b0 2b82 64 c0 00"
  val string_request_local_1112_mid     = hex"b0 2c02 64 c0 00"
  val string_request_remote_12345_high  = hex"b0 9c98 01 c7 803131312e3232322e3132332e323334"

  "decode local 75 high" in {
    PacketCoding.decodePacket(string_request_local_75_high).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, server_ip, data) =>
        remote_host mustEqual false
        port mustEqual 75
        bandwidth mustEqual 3
        server_ip mustEqual ""
        data mustEqual ByteVector.empty
      case _ =>
        ko
    }
  }

  "encode local 75 high" in {
    val msg = VoiceHostRequest(remote_host = false, 75, 3, "", ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_75_high
  }

  "decode local 1111 mid" in {
    PacketCoding.decodePacket(string_request_local_1111_mid).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, server_ip, data) =>
        remote_host mustEqual false
        port mustEqual 1111
        bandwidth mustEqual 201
        server_ip mustEqual ""
        data mustEqual ByteVector.empty
      case _ =>
        ko
    }
  }

  "encode local 1111 mid" in {
    val msg = VoiceHostRequest(remote_host = false, 1111, 201, "", ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_1111_mid
  }

  "decode local 1112 mid" in {
    PacketCoding.decodePacket(string_request_local_1112_mid).require match {
      case VoiceHostRequest(remote_host, port, bandwidth, server_ip, data) =>
        remote_host mustEqual false
        port mustEqual 1112
        bandwidth mustEqual 201
        server_ip mustEqual ""
        data mustEqual ByteVector.empty
      case _ =>
        ko
    }
  }

  "encode local 1112 mid" in {
    val msg = VoiceHostRequest(remote_host = false, 1112, 201, "", ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_local_1112_mid
  }

  "decode remote 12345 high" in {
    PacketCoding.decodePacket(string_request_remote_12345_high).require match {
      case VoiceHostRequest(remote, port, codec, server_ip, data) =>
        remote mustEqual true
        port mustEqual 12345
        codec mustEqual 3
        server_ip mustEqual "111.222.123.234"
        data mustEqual ByteVector.empty
      case _ =>
        ko
    }
  }

  "encode remote 12345 high" in {
    val msg = VoiceHostRequest(remote_host = true, port = 12345, bandwidth = 3, remote_ip = "111.222.123.234", ByteVector.empty)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_request_remote_12345_high
  }
}
