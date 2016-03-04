package psforever.net

import java.nio.charset.Charset

import scodec.{DecodeResult, Err, Codec, Attempt}
import scodec.bits._
import scodec.codecs._
import scodec._
import shapeless._
import shapeless.ops.hlist.Prepend

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

// Crypto Packets
final case class ClientChallengeXchg(time : Long, challenge : ByteVector, p : ByteVector, g : ByteVector)
  extends PlanetSideCryptoPacket {
  def opcode = CryptoPacketOpcode.ClientChallengeXchg
  def encode = ClientChallengeXchg.encode(this)
}

object ClientChallengeXchg extends Marshallable[ClientChallengeXchg] {
  implicit val codec: Codec[ClientChallengeXchg] = (
    ("unknown" | constant(1)) ::
      ("unknown" | constant(1)) ::
      ("client_time" | uint32L) ::
      ("challenge" | bytes(12)) ::
      ("end_chal?" | constant(0)) ::
      ("objects?" | constant(1)) ::
      ("object_type?" | constant(hex"0002".bits)) ::
      ("unknown" | constant(hex"ff240000".bits)) ::
      ("P_len" | constant(hex"1000".bits)) ::
      ("P" | bytes(16)) ::
      ("G_len" | constant(hex"1000".bits)) ::
      ("G" | bytes(16)) ::
      ("end?" | constant(0)) ::
      ("end?" | constant(0)) ::
      ("objects?" | constant(1)) ::
      ("unknown" | constant(hex"03070000".bits)) ::
      ("end?" | constant(0))
    ).as[ClientChallengeXchg]
}

final case class ServerChallengeXchg(time : Long, challenge : ByteVector, pubKey : ByteVector)
  extends PlanetSideCryptoPacket {
  type Packet = ServerChallengeXchg
  def opcode = CryptoPacketOpcode.ServerChallengeXchg
  def encode = ServerChallengeXchg.encode(this)
}

object ServerChallengeXchg extends Marshallable[ServerChallengeXchg] {
  def getCompleteChallenge(time : Long, rest : ByteVector): ByteVector =
    uint32L.encode(time).require.toByteVector ++ rest

  implicit val codec: Codec[ServerChallengeXchg] = (
    ("unknown" | constant(2)) ::
      ("unknown" | constant(1)) ::
      ("server_time" | uint32L) ::
      ("challenge" | bytes(0xC)) ::
      ("end?" | constant(0)) ::
      ("objects" | constant(1)) ::
      ("unknown" | constant(hex"03070000000c00".bits)) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(0x0e))
    ).as[ServerChallengeXchg]
}

final case class ClientFinished(pubKey : ByteVector, challengeResult: ByteVector)
  extends PlanetSideCryptoPacket {
  type Packet = ClientFinished
  def opcode = CryptoPacketOpcode.ClientFinished
  def encode = ClientFinished.encode(this)
}

object ClientFinished extends Marshallable[ClientFinished] {
  implicit val codec : Codec[ClientFinished] = (
    ("obj_type?" | constant(hex"10".bits)) ::
      ("pub_key_len" | constant(hex"1000")) ::
      ("pub_key" | bytes(16)) ::
      ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
    ).as[ClientFinished]
}

final case class ServerFinished(challengeResult : ByteVector)
  extends PlanetSideCryptoPacket {
  type Packet = ServerFinished
  def opcode = CryptoPacketOpcode.ServerFinished
  def encode = ServerFinished.encode(this)
}

object ServerFinished extends Marshallable[ServerFinished] {
  implicit val codec : Codec[ServerFinished] = (
    ("unknown" | constant(hex"0114".bits)) ::
      ("challenge_result" | bytes(0xc))
    ).as[ServerFinished]
}

// Game Packets
final case class LoginMessage(majorVersion : Long,
                              minorVersion : Long,
                              buildDate : String,
                              username : String,
                              password : Option[String],
                              token : Option[String],
                              revision : Long) extends PlanetSideGamePacket {
  require(majorVersion >= 0)
  require(minorVersion >= 0)
  require(revision >= 0)
  require(password.isDefined ^ token.isDefined, "Either 'username' or 'token' must be set, but not both")

  def opcode = GamePacketOpcode.LoginMessage
  def encode = LoginMessage.encode(this)
}

object LoginMessage extends Marshallable[LoginMessage] {
  private def username = PacketHelpers.encodedStringAligned(7)
  private def password = PacketHelpers.encodedString
  private def tokenPath = fixedSizeBytes(32, ascii) :: username
  private def passwordPath = username :: password

  type Struct = String :: Option[String] :: Option[String] :: HNil

  /* Okay, okay, here's what's happening here:

     PlanetSide's *wonderful* packet design reuses packets for different encodings.
     What we have here is that depending on a boolean in the LoginPacket, we will either
     be decoding a username & password OR a token & username. Yeah...so this doesn't
     really fit in to a fixed packet decoding scheme.

     The below code abstracts away from this by using pattern matching.
     The scodec specific part is the either(...) Codec, which decodes one bit and chooses
     Left or Right depending on it.
   */
  implicit val credentialChoice : Codec[Struct] = {
    type InStruct = Either[String :: String :: HNil, String :: String :: HNil]

    def from(a : InStruct) : Struct = a match {
      case Left(username :: password :: HNil) => username :: Some(password) :: None :: HNil
      case Right(token :: username :: HNil) => username :: None :: Some(token) :: HNil
    }

    // serialization can fail if the user did not specify a token or password (or both)
    def to(a : Struct) : InStruct = a match {
      case username :: Some(password) :: None :: HNil => Left(username :: password :: HNil)
      case username :: None :: Some(token) :: HNil => Right(token :: username :: HNil)
    }

    either(bool, passwordPath, tokenPath).xmap[Struct](from, to)
  }

  implicit val codec : Codec[LoginMessage] = (
    ("major_version" | uint32L) ::
    ("minor_version" | uint32L) ::
    ("build_date" | PacketHelpers.encodedString) ::
    (
      // The :+ operator (and the parens) are required because we are adding an HList to an HList,
      // not merely a value (like bool). Weird shit, but hey this works.
      ("credential_choice" | credentialChoice) :+
      ("revision" | uint32L)
    )
  ).as[LoginMessage]
}

final case class LoginRespMessage(token : String, // printable ascii for 16
                                  unknown : ByteVector, // hex"00000000 18FABE0C 00000000 00000000"
                                  error : Long, // 0
                                  stationError : Long, // 1
                                  subscriptionStatus : Long, // 2 or 5
                                  someToken : Long, // 685276011
                                  username : String, // the user
                                  unk5 : Long, // 0 and unset bool
                                  someBit : Boolean) extends PlanetSideGamePacket {
  def opcode = GamePacketOpcode.LoginRespMessage
  def encode = LoginRespMessage.encode(this)
}

object LoginRespMessage extends Marshallable[LoginRespMessage] {
  implicit val codec : Codec[LoginRespMessage] = (
    ("token" | fixedSizeBytes(16, ascii)) ::
    ("unknown" | bytes(16)) ::
    ("error" | uint32L) ::
    ("station_error" | uint32L) ::
    ("subscription_status" | uint32L) ::
    ("unknown" | uint32L) ::
    ("username" | PacketHelpers.encodedString) ::
    ("unknown" | uint32L) ::
    ("unknown" | byteAligned(bool))
    ).as[LoginRespMessage]
}

final case class ConnectToWorldMessage(world : String)
  extends PlanetSideGamePacket {
  type Packet = ConnectToWorldMessage
  def opcode = GamePacketOpcode.ConnectToWorldMessage
  def encode = ConnectToWorldMessage.encode(this)
}

object ConnectToWorldMessage extends Marshallable[ConnectToWorldMessage] {
  implicit val codec : Codec[ConnectToWorldMessage] = ascii.as[ConnectToWorldMessage]
}

// Control Packets
final case class HandleGamePacket(packet : ByteVector)
  extends PlanetSideControlPacket {
  def opcode = ControlPacketOpcode.HandleGamePacket
  def encode = throw new Exception("This packet type should never be encoded")
}

object HandleGamePacket extends Marshallable[HandleGamePacket] {
  implicit val codec : Codec[HandleGamePacket] = bytes.as[HandleGamePacket].decodeOnly
}

final case class ClientStart(clientNonce : Long)
  extends PlanetSideControlPacket {
  type Packet = ClientStart
  def opcode = ControlPacketOpcode.ClientStart
  def encode = ClientStart.encode(this)
}

object ClientStart extends Marshallable[ClientStart] {
  implicit val codec : Codec[ClientStart] = (
    ("unknown" | constant(hex"00000002".bits)) ::
      ("client_nonce" | uint32L) ::
      ("unknown" | constant(hex"000001f0".bits))
    ).as[ClientStart]
}

final case class ServerStart(clientNonce : Long, serverNonce : Long)
  extends PlanetSideControlPacket {
  type Packet = ServerStart
  def opcode = ControlPacketOpcode.ServerStart
  def encode = ServerStart.encode(this)
}

object ServerStart extends Marshallable[ServerStart] {
  implicit val codec : Codec[ServerStart] = (
    ("client_nonce" | uint32L) ::
      ("server_nonce" | uint32L) ::
      ("unknown" | constant(hex"000000000001d300000002".bits))
    ).as[ServerStart]
}

final case class MultiPacket(packets : Vector[ByteVector])
  extends PlanetSideControlPacket {
  type Packet = MultiPacket
  def opcode = ControlPacketOpcode.MultiPacket
  def encode = MultiPacket.encode(this)
}

object MultiPacket extends Marshallable[MultiPacket] {
  implicit val codec : Codec[MultiPacket] = ("packets" | vector(variableSizeBytes(uint8L, bytes))).as[MultiPacket]
}

final case class SlottedMetaPacket(/*slot : Int,*/ packet : ByteVector)
  extends PlanetSideControlPacket {
  type Packet = SlottedMetaPacket

  //assert(slot >= 0 && slot <= 7, "Slot number is out of range")

  def opcode = {
    val base = ControlPacketOpcode.SlottedMetaPacket0.id
    ControlPacketOpcode(base/* + slot*/)
  }

  def encode = SlottedMetaPacket.encode(this)
}

object SlottedMetaPacket extends Marshallable[SlottedMetaPacket] {
  implicit val codec : Codec[SlottedMetaPacket] = (
    ("unknown" | constant(0)) ::
    ("unknown" | constant(0)) ::
    ("rest" | bytes)
  ).as[SlottedMetaPacket]
}

final case class ConnectionClose()
  extends PlanetSideControlPacket {
  type Packet = ConnectionClose
  def opcode = ControlPacketOpcode.ConnectionClose
  def encode = ConnectionClose.encode(this)
}

object ConnectionClose extends Marshallable[ConnectionClose] {
  implicit val codec: Codec[ConnectionClose] = PacketHelpers.emptyCodec(ConnectionClose())
}


/////////////////////////////////////////////////////////////////

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


  def createEnumerationCodec[E <: Enumeration](enum : E, storageCodec : Codec[Int]) : Codec[E#Value] = {
    type Struct = Int :: HNil
    val struct: Codec[Struct] = storageCodec.hlist

    // Assure that the enum will always be able to fit in a N-bit int
    assert(enum.maxId <= Math.pow(storageCodec.sizeBound.exact.get, 2),
      enum.getClass.getCanonicalName + ": maxId exceeds primitive type")

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
  private def encodedStringSize : Codec[Int] = either(bool, uint(15), uint(7)).
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
