// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server to indicate a source of damage affecting the player.
  * Unlike `HitHint` the damage source is defined by an actual coordinate location rather than a physical target.
  * Setting the position to the world origin, however,
  * can cause the damage tick mark to point towards the previous damaging entity in some situations.<br>
  * <br>
  * The player will be shown a fading, outwards drifting, red tick mark.
  * The location will indicate a general direction towards the source.
  * If the option `Game/Show Damage Flash` is set, the player's screen will flash red briefly when a mark is displayed.
  * @param unk the intensity of the damage tick marks
  * @param pos the position
  * @see `HitHint`
  */
final case class DamageWithPositionMessage(unk: Int, pos: Vector3) extends PlanetSideGamePacket {
  type Packet = DamageWithPositionMessage
  def opcode = GamePacketOpcode.DamageWithPositionMessage
  def encode = DamageWithPositionMessage.encode(this)
}

object DamageWithPositionMessage extends Marshallable[DamageWithPositionMessage] {
  implicit val codec: Codec[DamageWithPositionMessage] = (
    ("unk" | uint8L) ::
      ("pos" | Vector3.codec_pos)
  ).xmap[DamageWithPositionMessage] (
    {
      case unk :: pos :: HNil =>
        DamageWithPositionMessage(math.max(0, math.min(unk, 255)), pos)
    },
    {
      case DamageWithPositionMessage(unk, pos) =>
        unk :: pos :: HNil
    }
  )
}
