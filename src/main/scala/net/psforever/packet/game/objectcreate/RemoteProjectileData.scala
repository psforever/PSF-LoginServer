// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.bits.{BitVector, ByteVector}
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.codecs._

object RemoteProjectiles {
  abstract class Data(val a: Int, val b: Int)

  final case object Meteor                          extends Data(0, 32)
  final case object Wasp                            extends Data(0, 208)
  final case object Sparrow                         extends Data(13107, 187)
  final case object PeregrineSparrow                extends Data(13107, 187)
  final case object OICW                            extends Data(13107, 195)
  final case object Striker                         extends Data(26214, 134)
  final case object HunterSeeker                    extends Data(39577, 201)
  final case object Starfire                        extends Data(39577, 249)
  final case object AphelionStarfire                extends Data(39577, 249)

  //the oicw_little_buddy is handled by its own transcoder
}

object FlightPhysics extends Enumeration {
  type Type = Value

  //seen in retail projectile creates
  val State0: FlightPhysics.Value = Value(0)
  //seen in retail projectile creates
  val State1: FlightPhysics.Value = Value(1)
  //seen in retail projectile creates
  val State2: FlightPhysics.Value = Value(2)
  //valid (extremely small distance) (requires non-zero unk4, unk5)
  val State3: FlightPhysics.Value = Value(3)
  //valid (infinite) (if unk4 == 0 unk5 == 0, minimum distance + time)
  val State4: FlightPhysics.Value = Value(4)
  //valid(infinite)
  val State5: FlightPhysics.Value = Value(5)
  //valid (uses velocity) (infinite)
  val State6: FlightPhysics.Value = Value(6)
  //valid (uses velocity) (infinite)
  val State7: FlightPhysics.Value = Value(7)
  //defined to allow retail decode of previously unknown projectile states
  val State8: FlightPhysics.Value = Value(8)
  //defined to allow retail decode of previously unknown projectile states
  val State9: FlightPhysics.Value = Value(9)
  //defined to allow retail decode of previously unknown projectile states
  val State10: FlightPhysics.Value = Value(10)
  //defined to allow retail decode of previously unknown projectile states
  val State11: FlightPhysics.Value = Value(11)
  //defined to allow retail decode of previously unknown projectile states
  val State12: FlightPhysics.Value = Value(12)
  //defined to allow retail decode of previously unknown projectile states
  val State13: FlightPhysics.Value = Value(13)
  //defined to allow retail decode of previously unknown projectile states
  val State14: FlightPhysics.Value = Value(14)
  //valid (uses velocity) (time > 0 is infinite) (unk5 == 2)
  val State15: FlightPhysics.Value = Value(15)

  implicit val codec: Codec[FlightPhysics.Value] = PacketHelpers.createEnumerationCodec(this, uint4)
}

/**
  * A representation of a projectile that the server must intentionally convey to players other than the shooter.
  * @param common_data common game object information
  * @param u1 na;
  *           first part of the canned remote projectile data
  * @param u2 na;
  *           second part of the canned remote projectile data
  * @param unk3 na;
  *             does something to how the projectile flies
  * @param unk4 na
  * @param unk5 na
  */
final case class RemoteProjectileData(
    common_data: CommonFieldDataWithPlacement,
    u1: Int,
    u2: Int,
    unk3: FlightPhysics.Value,
    unk4: Int,
    unk5: Int
) extends ConstructorData {
  override def bitsize: Long = 33L + common_data.bitsize
}

object RemoteProjectileData extends Marshallable[RemoteProjectileData] {
  private val TailBits = 33
  private val RemoteProjectileStartBitOffset = 4

  private final case class TailCursor(bytes: Array[Byte], var bitPos: Int) {
    private def byteIndex: Int = bitPos / 8
    private def bitIndexBE: Int = 7 - (bitPos % 8)
    private def bitIndexLE: Int = bitPos % 8

    def readBE(bits: Int): Int =
      (0 until bits).foldLeft(0) { (acc, _) =>
        val value = (bytes(byteIndex) >> bitIndexBE) & 1
        bitPos += 1
        (acc << 1) | value
      }

    def readLE(bits: Int): Int = {
      var out = 0
      for (i <- 0 until bits) {
        out |= ((bytes(byteIndex) >> bitIndexLE) & 1) << i
        bitPos += 1
      }
      out
    }

    def writeBE(value: Int, bits: Int): Unit =
      for (i <- (bits - 1) to 0 by -1) {
        if (((value >> i) & 1) != 0) {
          bytes(byteIndex) = (bytes(byteIndex) | (1 << bitIndexBE)).toByte
        }
        bitPos += 1
      }

    def writeLE(value: Int, bits: Int): Unit =
      for (i <- 0 until bits) {
        if (((value >> i) & 1) != 0) {
          bytes(byteIndex) = (bytes(byteIndex) | (1 << bitIndexLE)).toByte
        }
        bitPos += 1
      }
  }

  private def decodeTailBitOffset(commonData: CommonFieldDataWithPlacement): Int =
    ((RemoteProjectileStartBitOffset + commonData.bitsize) % 8).toInt

  private def decodeTail(bits: BitVector, startBitOffset: Int): Attempt[(Int, Int, FlightPhysics.Value, Int, Int)] = {
    if (bits.sizeLessThan(TailBits)) {
      Attempt.failure(Err.insufficientBits(TailBits, bits.size))
    } else {
      val chunk  = bits.take(TailBits)
      val padded = Array.fill[Byte](5)(0)
      val stream = TailCursor(padded, startBitOffset)
      for (i <- 0 until TailBits) {
        stream.writeBE(if (chunk.get(i.toLong)) 1 else 0, 1)
      }

      val cursor = TailCursor(padded, startBitOffset)
      val u1     = cursor.readBE(16)
      val u2     = cursor.readBE(8)
      val u3Raw  = cursor.readLE(4)
      val u4     = cursor.readBE(3)
      val u5     = cursor.readBE(2)

      if (u3Raw < FlightPhysics.values.firstKey.id || u3Raw >= FlightPhysics.maxId) {
        Attempt.failure(Err(s"Expected ${FlightPhysics} with ID between [${FlightPhysics.values.firstKey.id}, ${FlightPhysics.maxId - 1}], but got '$u3Raw'"))
      } else {
        Attempt.successful((u1, u2, FlightPhysics(u3Raw), u4, u5))
      }
    }
  }

  private def encodeTail(u1: Int, u2: Int, u3: FlightPhysics.Value, u4: Int, u5: Int, startBitOffset: Int): BitVector = {
    val padded = Array.fill[Byte](5)(0)
    val cursor = TailCursor(padded, startBitOffset)
    cursor.writeBE(u1, 16)
    cursor.writeBE(u2, 8)
    cursor.writeLE(u3.id, 4)
    cursor.writeBE(u4, 3)
    cursor.writeBE(u5, 2)
    BitVector(ByteVector(padded)).drop(startBitOffset).take(TailBits)
  }

  implicit val codec: Codec[RemoteProjectileData] = new Codec[RemoteProjectileData] {
    override def sizeBound: SizeBound = CommonFieldDataWithPlacement.codec.sizeBound + SizeBound.exact(TailBits)

    override def decode(bits: BitVector): Attempt[DecodeResult[RemoteProjectileData]] =
      CommonFieldDataWithPlacement.codec.decode(bits).flatMap { decoded =>
        val data = decoded.value
        decodeTail(decoded.remainder, decodeTailBitOffset(data)).map { case (u1, u2, u3, u4, u5) =>
          DecodeResult(RemoteProjectileData(data, u1, u2, u3, u4, u5), decoded.remainder.drop(TailBits))
        }
      }

    override def encode(value: RemoteProjectileData): Attempt[BitVector] =
      CommonFieldDataWithPlacement.codec.encode(value.common_data).map { commonBits =>
        val prefixSize = RemoteProjectileStartBitOffset
        val totalBits  = prefixSize + commonBits.size.toInt + TailBits
        val padded     = Array.fill[Byte]((totalBits + 7) / 8)(0.toByte)
        val stream     = TailCursor(padded, 0)

        for (_ <- 0 until prefixSize) {
          stream.writeBE(0, 1)
        }
        for (i <- 0 until commonBits.size.toInt) {
          stream.writeBE(if (commonBits.get(i.toLong)) 1 else 0, 1)
        }
        stream.writeBE(value.u1, 16)
        stream.writeBE(value.u2, 8)
        stream.writeLE(value.unk3.id, 4)
        stream.writeBE(value.unk4, 3)
        stream.writeBE(value.unk5, 2)

        val out = TailCursor(padded, prefixSize + commonBits.size.toInt)
        val tailBits = BitVector.bits((0 until TailBits).map(_ => out.readBE(1) != 0))
        commonBits ++ tailBits
      }
  }
}
