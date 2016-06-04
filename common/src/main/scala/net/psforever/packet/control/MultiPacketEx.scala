// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import scodec.bits._
import scodec.codecs._

final case class MultiPacketEx(packets : Vector[ByteVector])
  extends PlanetSideControlPacket {
  type Packet = MultiPacketEx
  def opcode = ControlPacketOpcode.MultiPacketEx
  def encode = MultiPacketEx.encode(this)
}

object MultiPacketEx extends Marshallable[MultiPacketEx] {
  val ffFound: Codec[Boolean] = new Codec[Boolean] {
    def sizeBound = SizeBound.exact(0)
    def encode(b: Boolean) =
      if(b)
        Attempt.successful(hex"ff".bits)
      else
        Attempt.successful(bin"")
    def decode(b: BitVector) = {
      if (b.length >= 8 && b.take(8) == hex"ff".bits)
        Attempt.successful(DecodeResult(true, b.drop(8)))
      else
        Attempt.successful(DecodeResult(false, b))
    }

    override def toString = "bitsRemaining"
  }

  val twoffFound: Codec[Boolean] = new Codec[Boolean] {
    def sizeBound = SizeBound.exact(0)
    def encode(b: Boolean) =
      if(b)
        Attempt.successful(hex"ffff".bits)
      else
        Attempt.successful(bin"")
    def decode(b: BitVector) = {
      if (b.length >= 16 && b.take(16) == hex"ffff".bits)
        Attempt.successful(DecodeResult(true, b.drop(16)))
      else
        Attempt.successful(DecodeResult(false, b))
    }

    override def toString = "bitsRemaining"
  }

  val sizeCodec = either(ffFound, uint8L,
    either(twoffFound, uint16L, uint32L).xmap[Long](
      (a : Either[Int, Long]) => a.fold[Long](a => a, a => a),
      (a : Long) =>
        if(a < 0xffff) Left(a.toInt) else Right(a)
    )
  ).xmap[Long](
    (a : Either[Int, Long]) => a.fold[Long](a => a, a => a),
    (a : Long) =>
      if(a < 0xff) Left(a.toInt) else Right(a)
    )

  implicit val codec : Codec[MultiPacketEx] = (
    ("packets" | vector(variableSizeBytesLong(sizeCodec, bytes)))
    ).as[MultiPacketEx]
}