// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec

/**
  * na
  */
final case class BeginZoningMessage()
  extends PlanetSideGamePacket {
  type Packet = BeginZoningMessage
  def opcode = GamePacketOpcode.BeginZoningMessage
  def encode = BeginZoningMessage.encode(this)
}

object BeginZoningMessage extends Marshallable[BeginZoningMessage] {
  implicit val codec : Codec[BeginZoningMessage] = PacketHelpers.emptyCodec(BeginZoningMessage())
}
