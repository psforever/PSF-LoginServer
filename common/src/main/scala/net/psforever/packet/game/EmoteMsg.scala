// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.EmoteType
import scodec.Codec
import scodec.codecs._

final case class EmoteMsg(avatar_guid : PlanetSideGUID,
                          emote : EmoteType.Value)
  extends PlanetSideGamePacket {
  type Packet = EmoteMsg
  def opcode = GamePacketOpcode.EmoteMsg
  def encode = EmoteMsg.encode(this)
}

object EmoteMsg extends Marshallable[EmoteMsg] {
  implicit val codec : Codec[EmoteMsg] = (
      ("avatar_guid" | PlanetSideGUID.codec) ::
        ("emote" | EmoteType.codec)
    ).as[EmoteMsg]
}
