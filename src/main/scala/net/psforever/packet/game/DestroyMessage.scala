// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

final case class DestroyMessage(
                                 victim_guid: PlanetSideGUID,
                                 killer_guid: PlanetSideGUID,
                                 weapon_guid: PlanetSideGUID,
                                 position: Vector3
                               ) extends PlanetSideGamePacket {
  type Packet = DestroyMessage
  def opcode = GamePacketOpcode.DestroyMessage
  def encode = DestroyMessage.encode(this)
}

object DestroyMessage extends Marshallable[DestroyMessage] {
  implicit val codec: Codec[DestroyMessage] = (
    ("victim_guid" | PlanetSideGUID.codec) ::
      ("killer_guid" | PlanetSideGUID.codec) ::
      ("weapon_guid" | PlanetSideGUID.codec) ::
      ("position" | Vector3.codec_pos)
    ).as[DestroyMessage]
}
