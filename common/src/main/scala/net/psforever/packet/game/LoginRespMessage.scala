// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

final case class LoginRespMessage(token : String, // printable ascii for 16
                                  unknown : ByteVector, // hex"00000000 18FABE0C 00000000 00000000"
                                  error : Long, // 0
                                  stationError : Long, // 1
                                  subscriptionStatus : Long, // 2 or 5
                                  someToken : Long, // 685276011
                                  username : String, // the user
                                  privilege : Long) extends PlanetSideGamePacket {
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
    ("privilege" | uint32L)
      .flatZip(priv => bool)
      .xmap[Long]({case (a, _) => a}, priv => (priv, (priv & 1) == 1))
    ).as[LoginRespMessage]
}