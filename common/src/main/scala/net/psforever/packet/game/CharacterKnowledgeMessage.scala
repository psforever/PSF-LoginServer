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
    permissions: Set[Certification],
    unk1: Int,
    unk2: Int,
    unk3: PlanetSideGUID
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

  private val inverter: Codec[Boolean] = bool.xmap[Boolean](
    state => !state,
    state => !state
  )

  private val info_codec: Codec[CharacterKnowledgeInfo] = (
    ("name" | PacketHelpers.encodedWideStringAligned(adjustment = 7)) ::
      ("permissions" | ulongL(bits = 46)) ::
      ("unk1" | uint(bits = 6)) ::
      ("unk2" | uint(bits = 3)) ::
      ("unk3" | PlanetSideGUID.codec)
  ).xmap[CharacterKnowledgeInfo](
    {
      case name :: permissions :: u1 :: u2 :: u3 :: HNil =>
        CharacterKnowledgeInfo(name, Certification.fromEncodedLong(permissions), u1, u2, u3)
    },
    {
      case CharacterKnowledgeInfo(name, permissions, u1, u2, u3) =>
        name :: Certification.toEncodedLong(permissions) :: u1 :: u2 :: u3 :: HNil
    }
  )

  implicit val codec: Codec[CharacterKnowledgeMessage] = (
    ("char_id" | uint32L) ::
      ("info" | optional(inverter, info_codec))
  ).as[CharacterKnowledgeMessage]
}
