// Copyright (c) 2025 PSForever
package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The `OutfitMemberUpdate` packet (game opcode `0x91`) is sent by the world server to
  * record the *local player's own standing* in their outfit.
  *
  * '''Client behavior''' (static analysis of the shipped 3.15.84 client, decode `0x8a5a70`,
  * dispatch case `0x44e60a` calling `ClientOutfitManagerHandleOutfitMemberUpdate` at
  * `0x4c8090`): the packet is only applied when it names the outfit the local player
  * currently belongs to; the trailing flag and the member-character id/rank are stored
  * on the outfit entry (`+0xbc`/`+0xc0`/`+0xc4`), and the outfit window is refreshed.
  * The flag marks the outfit as the local player's own: the outfit browser uses it to
  * display the player's outfit row in its "your outfit" coloring when the listed
  * outfit matches (row filler `0x4c9ff0`).
  *
  * @param outfit_id the outfit of the local player (other values are ignored by the client)
  * @param char_id   the local player's char id within that outfit
  * @param rank      the local player's outfit rank (`0`-`7`)
  * @param flag      marks the outfit as the local player's own (the browser
  *                   highlights the matching outfit row only while this is set)
  */
final case class OutfitMemberUpdate(
    outfit_id: Long,
    char_id: Long,
    rank: Int, // 0-7
    flag: Boolean,
) extends PlanetSideGamePacket {
  type Packet = OutfitMemberUpdate
  def opcode = GamePacketOpcode.OutfitMemberUpdate
  def encode = OutfitMemberUpdate.encode(this)
}

object OutfitMemberUpdate extends Marshallable[OutfitMemberUpdate] {
  implicit val codec: Codec[OutfitMemberUpdate] = (
    ("outfit_id" | uint32L) ::
    ("char_id" | uint32L) ::
    ("rank" | uint(3)) ::
    ("flag" | bool)
  ).xmap[OutfitMemberUpdate](
    {
      case outfit_id :: char_id :: rank :: flag :: HNil =>
        OutfitMemberUpdate(outfit_id, char_id, rank, flag)
    },
    {
      case OutfitMemberUpdate(outfit_id, char_id, rank, flag) =>
        outfit_id :: char_id :: rank :: flag :: HNil
    }
  )
}
