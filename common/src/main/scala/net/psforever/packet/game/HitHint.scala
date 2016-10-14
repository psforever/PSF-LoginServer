// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param x this parameter should not exist
  */
final case class HitHint(unk1 : Int,
                         unk2 : Int,
                         x : Int)
  extends PlanetSideGamePacket {
  type Packet = HitHint
  def opcode = GamePacketOpcode.HitHint
  def encode = HitHint.encode(this)
}

object HitHint extends Marshallable[HitHint] {
  implicit val codec : Codec[HitHint] = (
    ("unk1" | uintL(10)) ::
      ("unk2" | uintL(10)) ::
      ("x" | uintL(12))
    ).as[HitHint]
}
