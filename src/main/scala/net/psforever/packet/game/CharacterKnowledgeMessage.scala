// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.objects.avatar.Certification
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class CharacterKnowledgeInfo(
    name: String,
    certifications: Set[Certification],
    unk1: Int,
    unk2: Int,
    zoneNumber: Int
)

final case class CharacterKnowledgeMessage(char_id: Long, info: Option[CharacterKnowledgeInfo])
    extends PlanetSideGamePacket {
  type Packet = CharacterKnowledgeMessage
  def opcode = GamePacketOpcode.CharacterKnowledgeMessage
  def encode = CharacterKnowledgeMessage.encode(this)
}

object CharacterKnowledgeMessage extends Marshallable[CharacterKnowledgeMessage] {
  def apply(char_id: Long): CharacterKnowledgeMessage =
    CharacterKnowledgeMessage(char_id, None)

  def apply(char_id: Long, info: CharacterKnowledgeInfo): CharacterKnowledgeMessage =
    CharacterKnowledgeMessage(char_id, Some(info))

  private val inverter: Codec[Boolean] = bool.xmap[Boolean](state => !state, state => !state)

  private val info_codec: Codec[CharacterKnowledgeInfo] = (
    ("name" | PacketHelpers.encodedWideStringAligned(adjustment = 7)) ::
      ("certifications" | ulongL(bits = 46)) ::
      ("unk1" | uint(bits = 6)) ::
      ("unk2" | uint(bits = 3)) ::
      ("zone" | uint16L)
  ).xmap[CharacterKnowledgeInfo](
    {
      case name :: certs :: u1 :: u2 :: zone :: HNil =>
        CharacterKnowledgeInfo(name, Certification.fromEncodedLong(certs), u1, u2, zone)
    },
    {
      case CharacterKnowledgeInfo(name, certs, u1, u2, zone) =>
        name :: Certification.toEncodedLong(certs) :: u1 :: u2 :: zone :: HNil
    }
  )

  implicit val codec: Codec[CharacterKnowledgeMessage] = (
    ("char_id" | uint32L) ::
      ("info" | optional(inverter, info_codec))
  ).as[CharacterKnowledgeMessage]
}
