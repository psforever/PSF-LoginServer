// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** The SquadFacilityBindInfoMessage packet (game opcode 0xE2) informs a client that
  * a squad's facility ("home base") bind changed.
  *
  * It is dispatched by the server only. The shipped client builds the message in its
  * constructor at 0x8949c0, decodes it at 0x894b60 (a one-bit flag followed by three
  * 32-bit values, exactly as encoded below) and applies it in the `SquadWaypointManager`
  * handler at 0x4d5190: the squad is looked up by `squadID`, the bind values are written
  * onto the squad record, and when the player's *own* squad is affected an additional
  * "home base bound" flag is set on the player's own squad member slot.
  *
  * @param unk0    na;
  *                always observed as `false` in recorded traffic;
  *                the client reads it but no effect on the receive path is confirmed
  * @param squadID identifier of the squad this message relates to;
  *               the client locates its squad record by this value
  * @param mapID   the "MapID" of the facility the squad leader is bound to;
  *                alternatively of the facility an AMS was deployed from
  * @param zoneID  zone identifier that `mapID` relates to, as in `zX` / `mapX`
  */
final case class SquadFacilityBindInfoMessage(
    unk0: Boolean,
    squadID: Long,
    mapID: Long,
    zoneID: Long
) extends PlanetSideGamePacket {
  type Packet = SquadFacilityBindInfoMessage
  def opcode = GamePacketOpcode.SquadFacilityBindInfoMessage
  def encode = SquadFacilityBindInfoMessage.encode(this)
}

object SquadFacilityBindInfoMessage extends Marshallable[SquadFacilityBindInfoMessage] {

  implicit val codec: Codec[SquadFacilityBindInfoMessage] = (
    ("unk0" | bool) ::
      ("squadID" | uint32L) ::
      ("mapID" | uint32L) ::
      ("zoneID" | uint32L)
  ).as[SquadFacilityBindInfoMessage]
}
