// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.bits._
import scodec.codecs._
import shapeless.HNil

final case class MultiPacketEx(packets : Vector[ByteVector])
  extends PlanetSideControlPacket {
  type Packet = MultiPacketEx
  def opcode = ControlPacketOpcode.MultiPacketEx
  def encode = MultiPacketEx.encode(this)
}

object MultiPacketEx extends Marshallable[MultiPacketEx] {
  val sizeCodec : Codec[Long] = new Codec[Long] {
    private def description = s"variable-bit unsigned integer"
    override def sizeBound = SizeBound.bounded(8, 32)

    val MaxValue = (1L << 31) - 1
    val MinValue = 0

    override def encode(i: Long) = {
      if (i > MaxValue) {
        Attempt.failure(Err(s"$i is greater than maximum value $MaxValue for $description"))
      } else if (i < MinValue) {
        Attempt.failure(Err(s"$i is less than minimum value $MinValue for $description"))
      } else {
        if(i < 0xff) {
          uint8L.encode(i.toInt)
        } else if(i < 0xffff) {
          (constant(hex"ff") :: uint16L).dropUnits.encode(i.toInt :: HNil)
        } else {
          (constant(hex"ffffff") :: uint32L).dropUnits.encode(i :: HNil)
        }
      }
    }

    override def decode(buffer: BitVector) : Attempt[DecodeResult[Long]] = {
      val sizeTypes = List(8, 16, 32)
      val guards = List(hex"ff".bits, hex"ffff".bits)

      var buf = buffer

      for(i <- sizeTypes.indices) {
        val s = sizeTypes{i}

        if(!buf.sizeGreaterThanOrEqual(s))
          return Attempt.failure(Err.insufficientBits(s, buf.size))

        val value = buf.take(s)
        buf = buf.drop(s)

        if(i == guards.length || value != guards{i})
          return Attempt.successful(
            DecodeResult(value.toLong(signed = false, ByteOrdering.LittleEndian),
              buf)
          )
      }

      // will never reach here
      Attempt.failure(Err("unknown error"))
    }

    override def toString = description
  }

  implicit val codec : Codec[MultiPacketEx] = ("packets" | vector(variableSizeBytesLong(sizeCodec, bytes))).as[MultiPacketEx]
}