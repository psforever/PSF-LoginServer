// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class SetCurrentAvatarMessage(guid: PlanetSideGUID, unk1: Int, unk2: Int) extends PlanetSideGamePacket {
  type Packet = SetCurrentAvatarMessage
  def opcode = GamePacketOpcode.SetCurrentAvatarMessage
  def encode = SetCurrentAvatarMessage.encode(this)
}

object SetCurrentAvatarMessage extends Marshallable[SetCurrentAvatarMessage] {
  implicit val codec: Codec[SetCurrentAvatarMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint(3)) ::
      ("unk2" | uint(3))
  ).as[SetCurrentAvatarMessage]
}
