// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

object CharacterGender extends Enumeration(1) {
  type Type = Value

  val Male, Female = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

/**
  * Is sent by the PlanetSide client on character selection completion.
  */
final case class CharacterCreateRequestMessage(name : String,
                                               headId : Int,
                                               voiceId : Int,
                                               gender : CharacterGender.Value,
                                               empire : PlanetSideEmpire.Value)
  extends PlanetSideGamePacket {
  type Packet = CharacterCreateRequestMessage
  def opcode = GamePacketOpcode.CharacterCreateRequestMessage
  def encode = CharacterCreateRequestMessage.encode(this)
}

object CharacterCreateRequestMessage extends Marshallable[CharacterCreateRequestMessage] {
  implicit val codec : Codec[CharacterCreateRequestMessage] = (
    ("name" | PacketHelpers.encodedWideString) ::
      ("headId" | uint8L) ::
      ("voiceId" | uint8L) ::
      ("gender" | CharacterGender.codec) ::
      ("empire" | PlanetSideEmpire.codec)
    ).as[CharacterCreateRequestMessage]
}