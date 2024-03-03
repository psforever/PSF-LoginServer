// Copyright (c) 2023 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
 * ...
 */
final case class AIDamage(
                           target_guid: PlanetSideGUID,
                           attacker_guid: PlanetSideGUID,
                           projectile_type: Long,
                           unk1: Long,
                           unk2: Long
                         ) extends PlanetSideGamePacket {
  type Packet = ActionResultMessage
  def opcode = GamePacketOpcode.AIDamage
  def encode = AIDamage.encode(this)
}

object AIDamage extends Marshallable[AIDamage] {
  implicit val codec: Codec[AIDamage] = (
    ("target_guid" | PlanetSideGUID.codec) ::
      ("attacker_guid" | PlanetSideGUID.codec) ::
      ("projectile_type" | ulongL(bits = 32)) ::
      ("unk1" | ulongL(bits = 32)) ::
      ("unk2" | ulongL(bits = 32))
    ).as[AIDamage]
}
