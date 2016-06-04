// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Is sent by the PlanetSide client when selecting a character to play from the character selection
  * menu.
  */
final case class CharacterRequestMessage(unk : Long, unk2 : Long)
  extends PlanetSideGamePacket {
  type Packet = CharacterRequestMessage
  def opcode = GamePacketOpcode.CharacterRequestMessage
  def encode = CharacterRequestMessage.encode(this)
}

object CharacterRequestMessage extends Marshallable[CharacterRequestMessage] {
  implicit val codec : Codec[CharacterRequestMessage] = (
      ("unk1" | uint32L) ::
        ("unk2" | uint32L)
    ).as[CharacterRequestMessage]
}