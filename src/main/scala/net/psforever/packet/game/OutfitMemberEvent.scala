// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class OutfitMemberEvent(
  unk00: Int,
  outfit_id: Long,
  member_id: Long,
  member_name: String,
  rank: Int, // 0-7
  points: Long, // client divides this by 100
  last_login: Long, // seconds ago from current time, 0 if online
  unk1: Int,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberEvent
  def opcode = GamePacketOpcode.OutfitMemberEvent
  def encode = OutfitMemberEvent.encode(this)
}

object OutfitMemberEvent extends Marshallable[OutfitMemberEvent] {
  implicit val codec: Codec[OutfitMemberEvent] = (
    ("unk00" | uintL(2)) ::
    ("outfit_id" | uint32L) ::
    ("member_id" | uint32L) ::
    ("member_name" | PacketHelpers.encodedWideStringAligned(6)) ::
    ("rank" | uint(3)) ::
    ("points" | uint32L) ::
    ("last_login" | uint32L) ::
    ("unk1" | uint(5))
    ).xmap[OutfitMemberEvent](
    {
      case unk00 :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: u1 :: HNil =>
        OutfitMemberEvent(unk00, outfit_id, member_id, member_name, rank, points, last_login, u1)
    },
    {
      case OutfitMemberEvent(unk00, outfit_id, member_id, member_name, rank, points, last_login, u1) =>
        unk00 :: outfit_id :: member_id :: member_name :: rank :: points :: last_login :: u1 :: HNil
    }
  )
}
