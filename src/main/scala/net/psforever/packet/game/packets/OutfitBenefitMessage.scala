// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}
import shapeless.{::, HNil}

/**
  * The `OutfitBenefitMessage` packet (game opcode `0xCA`) is sent by the world server to
  * the members of an outfit to announce an outfit-wide benefit.
  *
  * '''Client behavior''' (static analysis of the shipped 3.15.84 client, decode `0x8a4cc0`,
  * dispatch case `0x44e6d0`): the client looks the `outfit_id` up in the outfit registry
  * (`FUN_008a51c0` on the `ClientOutfitManager`); if - and only if - the local player is a
  * member of that outfit, it resolves the outfit entry to a localized zone name through the
  * zone-data manager (`ClientZoneDataManager::HandleOutfitEvent` at `0x515240`),
  * and shows a HUD notification (`FUN_00506da0`) combining the zone name with the
  * remaining 32-bit values of the message. The interaction of those values with the
  * display is not fully documented; no stock capture of this message is on record,
  * the feature being unused by the stock servers.
  *
  * @param outfit_id na; the outfit whose members are being notified
  * @param unk1      na; the first value of the announcement (applied by the HUD display call)
  * @param unk2      na; the second value of the announcement (applied by the HUD display call)
  * @param unk3      na; the third value of the announcement (applied by the HUD display call)
  */
final case class OutfitBenefitMessage(
                                       outfit_id: Long,
                                       unk1: Long,
                                       unk2: Long,
                                       unk3: Long,
                                     ) extends PlanetSideGamePacket {
  type Packet = OutfitBenefitMessage
  def opcode: Type = GamePacketOpcode.OutfitBenefitMessage
  def encode: Attempt[BitVector] = OutfitBenefitMessage.encode(this)
}

object OutfitBenefitMessage extends Marshallable[OutfitBenefitMessage] {
  implicit val codec: Codec[OutfitBenefitMessage] = (
    ("outfit_id" | uint32L) ::
    ("unk1" | uint32L) ::
    ("unk2" | uint32L) ::
    ("unk3" | uint32L)
  ).xmap[OutfitBenefitMessage](
    {
      case outfit_id :: unk1 :: unk2 :: unk3 :: HNil =>
        OutfitBenefitMessage(outfit_id, unk1, unk2, unk3)
    },
    {
      case OutfitBenefitMessage(outfit_id, unk1, unk2, unk3) =>
        outfit_id :: unk1 :: unk2 :: unk3 :: HNil
    }
  )
}
