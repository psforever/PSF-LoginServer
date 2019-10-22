// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * This message is sent from the server to the client upon reception of a [[LoginMessage]].
  *
  * The result of the login is contained in this message. When a login is successful, a session token
  * is returned to the client which then forwards this to the World server it chooses to connect to.
  *
  * In terms of failed logins, the PS client favors errors in this order
  *
  * 1. LoginError
  * 2. StationError
  * 3. StationSubscriptionStatus
  *
  * Don't try and set more than one error at the same time. Just provide a single error message to be displayed.
  *
  * @param token A 'token' which acts exactly like a session cookie in a browser. Allows logins to not use a password
  * @param error A general login error message
  * @param stationError A PlanetSide Sony Online Entertainment (SOE) station result
  * @param subscriptionStatus A response detailing the current subscription type
  * @param unkUIRelated An unknown possible bitfield that controls some game variables (possibly expansions?)
  * @param username The login username
  * @param privilege If set above 10000, then the user has access to GM commands. Not sure of other values.
  */
final case class LoginRespMessage(token : String,
                                  error : LoginRespMessage.LoginError.Type,
                                  stationError : LoginRespMessage.StationError.Type,
                                  subscriptionStatus : LoginRespMessage.StationSubscriptionStatus.Type,
                                  unkUIRelated : Long,
                                  username : String,
                                  privilege : Long) extends PlanetSideGamePacket {
  def opcode = GamePacketOpcode.LoginRespMessage
  def encode = LoginRespMessage.encode(this)
}


object LoginRespMessage extends Marshallable[LoginRespMessage] {

  object LoginError extends Enumeration {
    type Type = Value
    val Success = Value(0)
    val unk1 = Value(1)
    val BadUsernameOrPassword = Value(5)
    val BadVersion = Value(0xf)

    implicit val codec = PacketHelpers.createLongEnumerationCodec(this, uint32L)
  }

  object StationError extends Enumeration {
    type Type = Value
    val AccountActive = Value(1)
    val AccountClosed = Value(2) // "Your Station account is currently closed"

    implicit val codec = PacketHelpers.createLongEnumerationCodec(this, uint32L)
  }

  object StationSubscriptionStatus extends Enumeration {
    type Type = Value
    val None = Value(1) // "You do not have a PlanetSide subscription"
    val Active = Value(2) /// Not sure about this one (guessing) (no ingame error message)
    val unk3 = Value(3)
    val Closed = Value(4) // "Your PlanetSide subscription is currently closed"
    val Trial = Value(5) /// Not sure about this one either (no ingame error message)
    val TrialExpired = Value(6) // "Your trial PlanetSide subscription has expired"

    implicit val codec = PacketHelpers.createLongEnumerationCodec(this, uint32L)
  }

  implicit val codec : Codec[LoginRespMessage] = (
    ("token" | LoginMessage.tokenCodec) ::
    ("error" | LoginError.codec) ::
    ("station_error" | StationError.codec) ::
    ("subscription_status" | StationSubscriptionStatus.codec) ::
    ("unknown" | uint32L) ::
    ("username" | PacketHelpers.encodedString) ::
    ("privilege" | uint32L)
      .flatZip(_ => bool) // really not so sure about this bool part. client gets just a single bit
      .xmap[Long]({case (a, _) => a}, priv => (priv, (priv & 1) == 1))
    ).as[LoginRespMessage]
}
