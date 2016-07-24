// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class EmoteMsg(avatar_guid : PlanetSideGUID,
                          emote_id : Int)
  extends PlanetSideGamePacket {
  type Packet = EmoteMsg
  def opcode = GamePacketOpcode.EmoteMsg
  def encode = EmoteMsg.encode(this)
}

object EmoteMsg extends Marshallable[EmoteMsg] {
  implicit val codec : Codec[EmoteMsg] = (
      ("avatar_guid" | PlanetSideGUID.codec) ::
        ("emote_id" | uint8L)
    ).as[EmoteMsg]
}
