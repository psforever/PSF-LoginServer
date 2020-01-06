// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class ChangeFireStateMessage_Stop(item_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = ChangeFireStateMessage_Stop
  def opcode = GamePacketOpcode.ChangeFireStateMessage_Stop
  def encode = ChangeFireStateMessage_Stop.encode(this)
}

object ChangeFireStateMessage_Stop extends Marshallable[ChangeFireStateMessage_Stop] {
  implicit val codec : Codec[ChangeFireStateMessage_Stop] = ("item_guid" | PlanetSideGUID.codec).as[ChangeFireStateMessage_Stop]
}
