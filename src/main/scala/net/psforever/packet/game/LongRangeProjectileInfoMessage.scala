// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

final case class LongRangeProjectileInfoMessage(
                                                 guid: PlanetSideGUID,
                                                 pos: Vector3,
                                                 vel: Option[Vector3]
                                               )
  extends PlanetSideGamePacket {
  type Packet = LongRangeProjectileInfoMessage
  def opcode = GamePacketOpcode.LongRangeProjectileInfoMessage
  def encode = LongRangeProjectileInfoMessage.encode(this)
}

object LongRangeProjectileInfoMessage extends Marshallable[LongRangeProjectileInfoMessage] {
  def apply(guid: PlanetSideGUID, pos: Vector3, vel: Vector3): LongRangeProjectileInfoMessage =
    LongRangeProjectileInfoMessage(guid, pos, Some(vel))

  implicit val codec: Codec[LongRangeProjectileInfoMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("pos" | Vector3.codec_pos) ::
    ("vel" | optional(bool, Vector3.codec_vel))
  ).as[LongRangeProjectileInfoMessage]
}
