// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param player_guid the player
  * @param friend the name of the friend
  * @param unk na
  */
final case class FriendsResponse(player_guid : PlanetSideGUID,
                                 friend : String,
                                 unk : Boolean)
  extends PlanetSideGamePacket {
  type Packet = FriendsResponse
  def opcode = GamePacketOpcode.FriendsResponse
  def encode = FriendsReponse.encode(this)
}

object FriendsReponse extends Marshallable[FriendsResponse] {
  implicit val codec : Codec[FriendsResponse] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("friend" | PacketHelpers.specSizeWideStringAligned(uint(5), 3)) ::
      ("unk" | bool)
    ).as[FriendsResponse]
}
