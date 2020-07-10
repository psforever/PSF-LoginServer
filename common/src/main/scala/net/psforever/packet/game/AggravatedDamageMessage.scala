// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param guid na
  * @param unk na
  */
final case class AggravatedDamageMessage(guid : PlanetSideGUID,
                                         unk : Long)
  extends PlanetSideGamePacket {
  type Packet = AggravatedDamageMessage
  def opcode = GamePacketOpcode.AggravatedDamageMessage
  def encode = AggravatedDamageMessage.encode(this)
}

object AggravatedDamageMessage extends Marshallable[AggravatedDamageMessage] {
  implicit val codec : Codec[AggravatedDamageMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("unk" | uint32L)
    ).as[AggravatedDamageMessage]
}
