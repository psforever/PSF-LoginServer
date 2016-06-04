// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import java.nio.charset.Charset

import scodec.{DecodeResult, Err, Codec, Attempt}
import scodec.bits._
import scodec.codecs._
import scodec._
import shapeless._

// Base packets
sealed trait PlanetSidePacket extends Serializable {
  def encode : Attempt[BitVector]
  def opcode : Enumeration#Value
}

// Used by companion objects to create encoders and decoders
trait Marshallable[T] {
  implicit val codec : Codec[T]
  def encode(a : T) : Attempt[BitVector] = codec.encode(a)
  // assert that when decoding a marshallable type, that no bits are left over
  def decode(a : BitVector) : Attempt[DecodeResult[T]] = codec.decode(a)
}

trait PlanetSideGamePacket extends PlanetSidePacket {
  def opcode : GamePacketOpcode.Type
}

trait PlanetSideControlPacket extends PlanetSidePacket {
  def opcode : ControlPacketOpcode.Type
}

trait PlanetSideCryptoPacket extends PlanetSidePacket {
  def opcode : CryptoPacketOpcode.Type
}

// Packet typing
final case class PlanetSidePacketFlags(packetType : PacketType.Value, secured : Boolean)

// Enumeration starts at 1
object PacketType extends Enumeration(1) {
  type Type = Value
  val ResetSequence, Unknown2, Crypto, Normal = Value

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint4L)
}

object PlanetSidePacketFlags extends Marshallable[PlanetSidePacketFlags] {
  implicit val codec : Codec[PlanetSidePacketFlags] = (
      ("packet_type" | PacketType.codec) ::
      ("unused" | constant(bin"0")) ::
      ("secured" | bool) ::
      ("advanced" | constant(bin"1")) :: // we only support "advanced packets"
      ("length_specified" | constant(bin"0")) // we DO NOT support this field
    ).as[PlanetSidePacketFlags]
}

//////////////////////////////////////////////////

/*class MarshallableEnum[+T] extends Enumeration {
  type StorageType = Codec[Int]

  implicit val storageType : StorageType = uint8

  assert(maxId <= Math.pow(storageType.sizeBound.exact.get, 2),
    this.getClass.getCanonicalName + ": maxId exceeds primitive type")

  implicit val codec: Codec[T] = PacketHelpers.createEnumerationCodec(this, storageType)
}*/

object PacketHelpers {
  def emptyCodec[T](instance : T) = {
    def to(pkt: T) = HNil
    def from(a: HNil) = instance
    Codec[HNil].xmap[T](from, to)
  }

  // NOTE: enumerations in scala can't be represented by more than an Int anyways, so this conversion shouldnt matter.
  // This is only to overload createEnumerationCodec to work with uint32[L] codecs (which are Long)
  def createLongEnumerationCodec[E <: Enumeration](enum : E, storageCodec : Codec[Long]) : Codec[E#Value] = {
    createEnumerationCodec(enum, storageCodec.xmap[Int](_.toInt, _.toLong))
  }

  def createEnumerationCodec[E <: Enumeration](enum : E, storageCodec : Codec[Int]) : Codec[E#Value] = {
    type Struct = Int :: HNil
    val struct: Codec[Struct] = storageCodec.hlist
    val primitiveLimit = Math.pow(2, storageCodec.sizeBound.exact.get)

    // Assure that the enum will always be able to fit in a N-bit int
    assert(enum.maxId <= primitiveLimit,
      enum.getClass.getCanonicalName + s": maxId exceeds primitive type (limit of $primitiveLimit, maxId ${enum.maxId})")

    def to(pkt: E#Value): Struct = {
      pkt.id :: HNil
    }

    def from(struct: Struct): Attempt[E#Value] = struct match {
      case enumVal :: HNil =>
        // verify that this int can match the enum
        val first = enum.values.firstKey.id
        val last = enum.maxId-1

        if(enumVal >= first && enumVal <= last)
          Attempt.successful(enum(enumVal))
        else
          Attempt.failure(Err(s"Expected ${enum} with ID between [${first}, ${last}], but got '${enumVal}'"))
    }

    struct.narrow[E#Value](from, to)
  }

  // when the first bit of the byte is set, the size can be between [0, 127].
  // otherwise, it is between [128, 32767] and two bytes are used for encoding
  // The magic in this is next level
  def encodedStringSize : Codec[Int] = either(bool, uint(15), uint(7)).
    xmap[Int](
    (a : Either[Int, Int]) => a.fold[Int](a => a, a => a),
    (a : Int) =>
      // if the specified goes above 0x7f (127) then we need two bytes to represent it
      if(a > 0x7f) Left(a) else Right(a)
  )

  /*private def encodedStringSizeWithLimit(limit : Int) : Codec[Int] = {
    either(bool, uint(15), uint(7)).
      exmap[Int](
        (a : Either[Int, Int]) => {
          val result = a.fold[Int](a => a, a => a)

          if(result > limit)
            Attempt.failure(Err(s"Encoded string exceeded byte limit of $limit"))
          else
            Attempt.successful(result)
        },
        (a : Int) => {
          if(a > limit)
            return Attempt.failure(Err("adsf"))
          //return Left(Attempt.failure(Err(s"Encoded string exceeded byte limit of $limit")))

          if(a > 0x7f)
            return Attempt.successful(Left(a))
          else
            Right(a)
        }
    )
  }*/

  private def encodedStringSizeWithPad(pad : Int) : Codec[Int] = encodedStringSize <~ ignore(pad)

  def encodedString : Codec[String] = variableSizeBytes(encodedStringSize, ascii)
  //def encodedStringWithLimit(limit : Int) : Codec[String] = variableSizeBytes(encodedStringSizeWithLimit(limit), ascii)
  def encodedStringAligned(adjustment : Int) : Codec[String] = variableSizeBytes(encodedStringSizeWithPad(adjustment), ascii)

  /// Variable for the charset that planetside uses for unicode
  val utf16 = string(Charset.forName("UTF-16LE"))

  /// An encoded *wide* string is twice the length of the given encoded size and half of the length of the
  /// input string. We use xmap to transform the encodedString codec as this change is just a division and multiply
  def encodedWideString : Codec[String] = variableSizeBytes(encodedStringSize.xmap(
    insize => insize*2,
    outSize => outSize/2), utf16)
}
