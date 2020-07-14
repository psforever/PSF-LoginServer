// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

final case class DestroyMessage(unk1: PlanetSideGUID, unk2: PlanetSideGUID, unk3: PlanetSideGUID, pos: Vector3)
    extends PlanetSideGamePacket {
  type Packet = DestroyMessage
  def opcode = GamePacketOpcode.DestroyMessage
  def encode = DestroyMessage.encode(this)
}

object DestroyMessage extends Marshallable[DestroyMessage] {
  implicit val codec: Codec[DestroyMessage] = (
    ("unk1" | PlanetSideGUID.codec) ::
      ("unk2" | PlanetSideGUID.codec) ::
      ("unk3" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos)
  ).as[DestroyMessage]
}
