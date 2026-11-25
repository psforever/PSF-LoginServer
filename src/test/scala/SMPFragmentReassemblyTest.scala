// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

import org.specs2.mutable._
import net.psforever.actors.net.SMPFragmentAssembler
import net.psforever.packet.PacketCoding
import net.psforever.packet.game.packets.{OutfitRequest, OutfitRequestAction}
import scodec.bits._
import scodec.codecs._

/**
  * The PlanetSide transport's fragment channel, per the shipped 3.15.84 client:
  * `SOEChannelProcessSlottedPacket` derives the channel from the `SlottedMetaPacket` slot as
  * `(opcode - 9) >> 2`; `SOEChannelDeliverInOrderPacket` then treats the channels differently -
  * channel 0 (slots 0-3) delivers each frame's contents as one complete message immediately,
  * while channel 1 (slots 4-7) reassembles a length-declared message spanning consecutive
  * frames: the opening frame carries a 4-byte big-endian `u32` with the size of the whole
  * message, and each frame contributes exactly the bytes still missing (over-delivery is
  * discarded, as the client's own reader does).
  *
  * The server-side `splitPacket` frame - a `00 00 <BE u16 size>` `HandleGamePacket` header - reads
  * as exactly that big-endian `u32` (the marker supplies the zero high bytes), so the outbound
  * and inbound schemes are the same wire format.
  */
class SMPFragmentReassemblyTest extends Specification {

  /** the raw client wire form of an `OutfitRequest` motd message, as emitted by the client's encoder */
  private def clientMotdPacket(t: String, id: Int = 5): ByteVector = {
    var out = uint8.encode(0x8e).require ++ uintL(3).encode(0).require ++ uint32L.encode(id).require
    val count = t.length
    out = out ++
      (if (count < 0x80) {
         uint8.encode(count | 0x80).require
       } else {
         uint8.encode((count >> 8) & 0xff).require ++ uint8.encode(count & 0xff).require
       })
    val rem = out.size % 8
    if (rem != 0) out = out ++ BitVector.high((8 - rem).toInt)
    (out ++ BitVector.view(t.getBytes("UTF-16LE"))).bytes
  }

  /** the opening frame of a fragmented message: [BE u32 size][first bytes] */
  private def leadingFrame(total: Long, first: ByteVector): ByteVector = {
    val header = ByteVector(
      ((total >> 24) & 0xff).toByte,
      ((total >> 16) & 0xff).toByte,
      ((total >> 8) & 0xff).toByte,
      (total & 0xff).toByte
    )
    header ++ first
  }

  "the fragment assembler" should {
    "dispatch a message that fits in a single fragment" in {
      val message = clientMotdPacket("messageoftheday")
      val assembler = new SMPFragmentAssembler(65536)

      assembler.append(leadingFrame(message.length, message)) mustEqual
        SMPFragmentAssembler.Completed(message, fragments = 1L)
    }

    "reassemble a message that spans several fragments" in {
      val message = clientMotdPacket("a" * 256) //256 wchars is the client editor's own cap; 520 B on the wire
      message.length mustEqual 520
      val slices  = message.grouped(300).toSeq
      slices.length mustEqual 2

      val assembler = new SMPFragmentAssembler(65536)
      val first = assembler.append(leadingFrame(message.length, slices.head))
      first mustEqual SMPFragmentAssembler.Waiting
      assembler.isActive mustEqual true
      assembler.declaredSize mustEqual message.length

      val second = assembler.append(slices.tail.head)
      second mustEqual SMPFragmentAssembler.Completed(message, fragments = 2L)
      assembler.isActive mustEqual false
    }

    "reassemble the exact multi-frame shape of the live incident" in {
      //the reported live failure: a client's maximal (256 wide character) outfit motd - 520 B on
      // the wire - split by the transport at its 459 B frame budget into 459 + 61 B frames
      val message = clientMotdPacket("a" * 256)
      message.length mustEqual 520
      val slices = message.grouped(459).toSeq
      slices.length mustEqual 2
      slices(0).length mustEqual 459
      slices(1).length mustEqual 61

      val assembler = new SMPFragmentAssembler(65536)
      //the leading frame: [BE u32 520][459 B of message], exactly as the client's encoder writes it
      assembler.append(leadingFrame(message.length, slices.head)) mustEqual SMPFragmentAssembler.Waiting
      //the 61 B tail frame
      assembler.append(slices(1)) mustEqual SMPFragmentAssembler.Completed(message, fragments = 2L)
    }

    "reassemble the outbound `splitPacket` framing (the HandleGamePacket u32 trick)" in {
      //the server's own splitPacket slices [00][00][BE u16 size][payload] across slot-4 frames;
      // the fragment-channel reader consumes the first four bytes as the BE u32 message size
      val packet  = OutfitRequest(5, OutfitRequestAction.Motd("Dear outfit: this message of the day is deliberately long enough to require a fragmented send. " * 4))
      val payload = PacketCoding.encodePacket(packet).require.toByteVector
      val wireHeaderAndPayload = hex"00 00" ++ ByteVector(
        ((payload.length >> 8) & 0xff).toByte,
        (payload.length & 0xff).toByte
      ) ++ payload

      val assembler = new SMPFragmentAssembler(65536)
      var result: SMPFragmentAssembler.Result = SMPFragmentAssembler.Waiting
      wireHeaderAndPayload.grouped(432).toSeq.foreach { slice => result = assembler.append(slice) }

      result must beLike {
        case SMPFragmentAssembler.Completed(message, fragments) =>
          fragments must beGreaterThan(1L)
          message mustEqual payload
          PacketCoding.decodePacket(message).require must beAnInstanceOf[OutfitRequest]
      }
    }

    "discard over-delivered bytes past the declared size, like the client's reader" in {
      val message = clientMotdPacket("Hello")
      val extra   = hex"deadbeef"
      val assembler = new SMPFragmentAssembler(65536)

      //the frame declares 9 bytes but carries 9 + 4: the trailing 4 are dropped, not carried into a next message
      assembler.append(leadingFrame(message.length, message ++ extra)) mustEqual
        SMPFragmentAssembler.Completed(message, fragments = 1L)
    }

    "reject a declared size beyond the reassembly limit and resynchronize on the next message" in {
      val assembler = new SMPFragmentAssembler(65536)

      val hostile = leadingFrame(total = 0x7fffffff, ByteVector.fill(455)(0x41.toByte))
      assembler.append(hostile) must beLike {
        case SMPFragmentAssembler.Rejected(_) => ok
      }
      assembler.isActive mustEqual false

      //a genuine message follows immediately and works
      val message = clientMotdPacket("messageoftheday")
      assembler.append(leadingFrame(message.length, message)) mustEqual
        SMPFragmentAssembler.Completed(message, fragments = 1L)
    }

    "reject a continuation fragment that arrives without its leader" in {
      val assembler = new SMPFragmentAssembler(65536)

      //a short mid-message tail with no length header (its leader was lost)
      val tail = hex"00 6e 00 73" ++ ByteVector.fill(29)(0x20.toByte)
      assembler.append(tail) must beLike {
        case SMPFragmentAssembler.Rejected(_) => ok
      }
      assembler.isActive mustEqual false
    }

    "reject a declared size of zero" in {
      val assembler = new SMPFragmentAssembler(65536)
      assembler.append(leadingFrame(0, hex"0000")) must beLike {
        case SMPFragmentAssembler.Rejected(_) => ok
      }
    }
  }

  "a plain-channel frame" should {
    "decode exactly as the server has always decoded it" in {
      //slots 0-3 never reassemble: the frame is one complete message or it is a permanent failure
      val message   = clientMotdPacket("messageoftheday")
      val frames    = message.grouped(459).toSeq //this message fits one frame; only the head would be sent
      val wholeFrame = frames.head

      PacketCoding.decodePacket(wholeFrame).require must beAnInstanceOf[OutfitRequest]

      //a frame of raw mid-message tail bytes - the shape that produced the original bug report's
      // "Expected ControlPacketOpcode ... got '49'" - is a decode failure, never a partial state
      val tailFrame = hex"00310020006f0066002000"
      PacketCoding.decodePacket(tailFrame) must beLike { case scodec.Attempt.Failure(_) => ok }
    }
  }
}
