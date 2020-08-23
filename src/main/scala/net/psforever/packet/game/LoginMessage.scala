// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless._

final case class LoginMessage(
    majorVersion: Long,
    minorVersion: Long,
    buildDate: String,
    username: String,
    password: Option[String],
    token: Option[String],
    revision: Long
) extends PlanetSideGamePacket {
  require(majorVersion >= 0)
  require(minorVersion >= 0)
  require(revision >= 0)
  require(password.isDefined ^ token.isDefined, "Either 'username' or 'token' must be set, but not both")

  def opcode = GamePacketOpcode.LoginMessage
  def encode = LoginMessage.encode(this)
}

object LoginMessage extends Marshallable[LoginMessage] {
  lazy val tokenCodec = paddedFixedSizeBytes(32, cstring, ignore(8))

  private def username     = PacketHelpers.encodedStringAligned(7)
  private def password     = PacketHelpers.encodedString
  private def tokenPath    = tokenCodec :: username
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
  implicit val credentialChoice: Codec[Struct] = {
    type InStruct = Either[String :: String :: HNil, String :: String :: HNil]

    def from(a: InStruct): Struct =
      a match {
        case Left(username :: password :: HNil) => username :: Some(password) :: None :: HNil
        case Right(token :: username :: HNil)   => username :: None :: Some(token) :: HNil
      }

    // serialization can fail if the user did not specify a token or password (or both)
    def to(a: Struct): InStruct =
      a match {
        case username :: Some(password) :: None :: HNil => Left(username :: password :: HNil)
        case username :: None :: Some(token) :: HNil    => Right(token :: username :: HNil)
      }

    either(bool, passwordPath, tokenPath).xmap[Struct](from, to)
  }

  implicit val codec: Codec[LoginMessage] = (
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
