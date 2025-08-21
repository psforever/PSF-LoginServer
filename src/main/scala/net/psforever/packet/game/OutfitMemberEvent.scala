// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
import shapeless.{::, HNil}

/*
  action is unimplemented! if action == 0 only outfit_id and member_id are sent
  action2 is unimplemented! if action2 == 0 unk2 will contain one additional uint32L
  unk2 contains one byte of padding. may contain 4byte of unknown data depending on action2
 */
final case class OutfitMemberEvent(
    action: Int, // action is unimplemented
    outfit_id: Long,
    member_id: Long,
    member_name: String,
    rank: Int, // 0-7
    points: Long, // client divides this by 100
    last_login: Long, // seconds ago from current time, 0 if online
    action2: Int, // this should always be 1, otherwise there will be actual data in unk2!
    padding: ByteVector, // only contains information if unk1 is 0, 1 byte of padding otherwise
  ) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent

  def opcode = GamePacketOpcode.OutfitMemberEvent

  def encode = OutfitMemberEvent.encode(this)
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {
  implicit val codec: Codec[OutfitMemberEvent] = (
    ("action" | uintL(2)) ::
      ("outfit_id" | uint32L) ::
      ("member_id" | uint32L) ::
      ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
      ("rank" | uint(3)) ::
      ("points" | uint32L) ::
      ("last_login" | uint32L) ::
      ("action2" | uintL(1)) ::
      ("padding" | bytes)
    ).xmap[OutfitMemberEvent](
    {
      case unk00 :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: u1 :: padding :: HNil =>
        OutfitMemberEvent(unk00, outfit_id, member_id, member_name, rank, points, last_login, u1, padding)
    },
    {
      case OutfitMemberEvent(unk00, outfit_id, member_id, member_name, rank, points, last_login, action2, padding) =>
        unk00 :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: action2 :: padding :: HNil
    }
  )
}
