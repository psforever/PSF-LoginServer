// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param seq_time na
  * @param player na
  * @param victim na
  * @param bullet na
  * @param pos na
  * @param unk1 na
  */
final case class LashMessage(seq_time : Int,
                             player : PlanetSideGUID,
                             victim : PlanetSideGUID,
                             bullet : PlanetSideGUID,
                             pos : Vector3,
                             unk1 : Int)
  extends PlanetSideGamePacket {
  type Packet = LashMessage
  def opcode = GamePacketOpcode.LashMessage
  def encode = LashMessage.encode(this)
}

object LashMessage extends Marshallable[LashMessage] {
  implicit val codec : Codec[LashMessage] = (
    ("seq_time" | uintL(10)) ::
      ("player" | PlanetSideGUID.codec) ::
      ("victim" | PlanetSideGUID.codec) ::
      ("bullet" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk1" | uintL(3))
    ).as[LashMessage]
}
