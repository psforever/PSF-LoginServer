// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * na<br>
  * The one common use of this packet is to transmit information about the name of the player's outfit during login.
  * The guid will belong to the player; the "type will be 0; the outfit name will appear on the appropriate window.
  * @param guid na
  * @param string_type na
  * @param string_value na
  */
final case class PlanetsideStringAttributeMessage(guid: PlanetSideGUID, string_type: Int, string_value: String)
    extends PlanetSideGamePacket {
  type Packet = PlanetsideStringAttributeMessage
  def opcode = GamePacketOpcode.PlanetsideStringAttributeMessage
  def encode = PlanetsideStringAttributeMessage.encode(this)
}

object PlanetsideStringAttributeMessage extends Marshallable[PlanetsideStringAttributeMessage] {
  implicit val codec: Codec[PlanetsideStringAttributeMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("string_type" | uint8L) ::
      ("string_value" | PacketHelpers.encodedWideString)
  ).as[PlanetsideStringAttributeMessage]
}
