// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class CharacterNoRecordMessage(unk: Long) extends PlanetSideGamePacket {
  type Packet = CharacterNoRecordMessage
  def opcode = GamePacketOpcode.CharacterNoRecordMessage
  def encode = CharacterNoRecordMessage.encode(this)
}

object CharacterNoRecordMessage extends Marshallable[CharacterNoRecordMessage] {
  implicit val codec: Codec[CharacterNoRecordMessage] = ("unk" | uint32L).as[CharacterNoRecordMessage]
}
