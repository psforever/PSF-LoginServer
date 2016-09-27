// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class AvatarImplantMessage(player_guid : PlanetSideGUID,
                                      unk1 : Int,
                                      unk2 : Int)
  extends PlanetSideGamePacket {
  type Packet = AvatarImplantMessage
  def opcode = GamePacketOpcode.AvatarImplantMessage
  def encode = AvatarImplantMessage.encode(this)
}

object AvatarImplantMessage extends Marshallable[AvatarImplantMessage] {
  implicit val codec : Codec[AvatarImplantMessage] = (
    ("plyaer_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
    ).as[AvatarImplantMessage]
}
