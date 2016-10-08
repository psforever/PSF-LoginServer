// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class FriendsRequest(unk : Int,
                                friend : String)
  extends PlanetSideGamePacket {
  type Packet = FriendsRequest
  def opcode = GamePacketOpcode.FriendsRequest
  def encode = FriendsRequest.encode(this)
}

object FriendsRequest extends Marshallable[FriendsRequest] {
  implicit val codec : Codec[FriendsRequest] = (
    ("unk" | uintL(3)) ::
      ("friend" | PacketHelpers.encodedWideStringAligned(5))
    ).as[FriendsRequest]
}

