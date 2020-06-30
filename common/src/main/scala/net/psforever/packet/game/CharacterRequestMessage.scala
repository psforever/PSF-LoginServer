// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

object CharacterRequestAction extends Enumeration(0) {
  type Type = Value
  val Select, Delete, Unused, Unknown3 = Value

  implicit val codec = PacketHelpers.createLongEnumerationCodec(this, uint32L)
}

/**
  * Is sent by the PlanetSide client when selecting a character to play from the character selection
  * menu.
  */
final case class CharacterRequestMessage(charId: Long, action: CharacterRequestAction.Type)
    extends PlanetSideGamePacket {
  type Packet = CharacterRequestMessage
  def opcode = GamePacketOpcode.CharacterRequestMessage
  def encode = CharacterRequestMessage.encode(this)
}

object CharacterRequestMessage extends Marshallable[CharacterRequestMessage] {
  implicit val codec: Codec[CharacterRequestMessage] = (
    ("charId" | uint32L) ::
      ("action" | CharacterRequestAction.codec)
  ).as[CharacterRequestMessage]
}
