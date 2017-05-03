// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class KeepAliveMessage(code : Int = 0) extends PlanetSideGamePacket {
  type Packet = KeepAliveMessage
  def opcode = GamePacketOpcode.KeepAliveMessage
  def encode = KeepAliveMessage.encode(this)
}

object KeepAliveMessage extends Marshallable[KeepAliveMessage] {
  implicit val codec : Codec[KeepAliveMessage] = ("keep_alive_code" | uint16L).as[KeepAliveMessage]
}