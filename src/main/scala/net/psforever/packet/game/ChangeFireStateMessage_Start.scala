// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class ChangeFireStateMessage_Start(item_guid: PlanetSideGUID) extends PlanetSideGamePacket {
  type Packet = ChangeFireStateMessage_Start
  def opcode = GamePacketOpcode.ChangeFireStateMessage_Start
  def encode = ChangeFireStateMessage_Start.encode(this)
}

object ChangeFireStateMessage_Start extends Marshallable[ChangeFireStateMessage_Start] {
  implicit val codec: Codec[ChangeFireStateMessage_Start] =
    ("item_guid" | PlanetSideGUID.codec).as[ChangeFireStateMessage_Start]
}
