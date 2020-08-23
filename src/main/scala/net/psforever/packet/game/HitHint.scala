// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to indicate a target or source of damage affecting the player.<br>
  * <br>
  * When a source is provided, and within render distance, the player will be shown a fading, outwards drifting, red tick mark.
  * The location and movement of the mark will indicate a general direction towards the source.
  * If the option `Game/Show Damage Flash` is set, the player's screen will flash red briefly when a mark is displayed.<br>
  * <br>
  * For while some mark is being displayed, the player will also make a grunt of pain.
  * @param source_guid the source of implied damage
  * @param player_guid the player
  */
final case class HitHint(source_guid: PlanetSideGUID, player_guid: PlanetSideGUID) extends PlanetSideGamePacket {
  type Packet = HitHint
  def opcode = GamePacketOpcode.HitHint
  def encode = HitHint.encode(this)
}

object HitHint extends Marshallable[HitHint] {
  implicit val codec: Codec[HitHint] = (
    ("source_guid" | PlanetSideGUID.codec) ::
      ("player_guid" | PlanetSideGUID.codec)
  ).as[HitHint]
}
