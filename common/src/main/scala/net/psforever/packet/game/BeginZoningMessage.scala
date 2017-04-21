// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec

/**
  * Dispatched by the client after the current map has been fully loaded locally and its objects are ready to be initialized.<br>
  * <br>
  * When the server receives the packet, for each object on that map, it sends the packets to the client:<br>
  * - `SetEmpireMessage`<br>
  * - `HackMessage`<br>
  * - `PlanetSideAttributeMessage`<br>
  * - ... and so forth<br>
  * Afterwards, an avatar POV is declared and the remaining details about the said avatar are assigned.
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
