// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.bits._
import scodec.codecs._
import shapeless.HNil

final case class MultiPacketEx(packets: Vector[ByteVector]) extends PlanetSideControlPacket {
  type Packet = MultiPacketEx
  def opcode = ControlPacketOpcode.MultiPacketEx
  def encode = MultiPacketEx.encode(this)
}

object MultiPacketEx extends Marshallable[MultiPacketEx] {
  val sizeCodec: Codec[Long] = new Codec[Long] {
    private def description = s"variable-bit unsigned integer"
    override def sizeBound  = SizeBound.bounded(8, 32)

    val MaxValue = (1L << 31) - 1
    val MinValue = 0

    /* Shared across calls: sizeCodec runs once per sub-packet of every inbound and outbound
       bundle, so these are built once here rather than per invocation. scodec codecs are
       immutable and safe to share. */
    private val mediumCodec = (constant(hex"ff") :: uint16L).dropUnits
    private val largeCodec  = (constant(hex"ffffff") :: uint32L).dropUnits
    private val sizeTypes   = Vector(8, 16, 32)
    private val guards      = Vector(hex"ff".bits, hex"ffff".bits)

    override def encode(i: Long) = {
      if (i > MaxValue) {
        Attempt.failure(Err(s"$i is greater than maximum value $MaxValue for $description"))
      } else if (i < MinValue) {
        Attempt.failure(Err(s"$i is less than minimum value $MinValue for $description"))
      } else {
        if (i < 0xff) {
          uint8L.encode(i.toInt)
        } else if (i < 0xffff) {
          mediumCodec.encode(i.toInt :: HNil)
        } else {
          largeCodec.encode(i :: HNil)
        }
      }
    }

    override def decode(buffer: BitVector): Attempt[DecodeResult[Long]] = {
      var buf = buffer

      for (i <- sizeTypes.indices) {
        val s = sizeTypes { i }

        if (!buf.sizeGreaterThanOrEqual(s))
          return Attempt.failure(Err.insufficientBits(s, buf.size))

        val value = buf.take(s)
        buf = buf.drop(s)

        if (i == guards.length || value != guards { i })
          return Attempt.successful(
            DecodeResult(value.toLong(signed = false, ByteOrdering.LittleEndian), buf)
          )
      }

      // will never reach here
      Attempt.failure(Err("unknown error"))
    }

    override def toString = description
  }

  implicit val codec: Codec[MultiPacketEx] =
    ("packets" | vector(variableSizeBytesLong(sizeCodec, bytes))).as[MultiPacketEx]
}
