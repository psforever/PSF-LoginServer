// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

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
    ).exmap[CharacterCreateRequestMessage] (
    {
      case name :: headId :: voiceId :: gender :: empire :: HNil =>
        Attempt.successful(CharacterCreateRequestMessage(name, headId, voiceId, gender, empire))
    },
    {
      case CharacterCreateRequestMessage(name, _, _, _, PlanetSideEmpire.NEUTRAL) =>
        Attempt.failure(Err(s"character $name's faction can not declare as neutral"))

      case CharacterCreateRequestMessage(name, headId, voiceId, gender, empire) =>
        Attempt.successful(name :: headId :: voiceId :: gender :: empire :: HNil)
    }
  )
}