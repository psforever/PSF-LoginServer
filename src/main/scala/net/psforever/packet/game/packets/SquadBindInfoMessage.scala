// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.game.packets.SquadBindInfoMessage.SquadBindEntry
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** The SquadBindInfoMessage packet (game opcode 0xE0) tells a client which facility
  * ("home base") each member of a squad is bound to.
  *
  * It is dispatched by the server only. The shipped client builds the message in its
  * constructor at 0x894420 (where `bindIndex` defaults to `-1`), decodes it at 0x8947d0,
  * and applies it in the `SquadWaypointManager` handler at 0x4d1020: for slots 0 through 9
  * it retrieves each slot's bind tuple, stores zone and map on the squad member record and
  * flags the member as bound, then refreshes the squad HUD.
  *
  * @param bindIndex the bind context the entries refer to;
  *                  `-1` (the client's own default) means "the squad's currently bound facility";
  *                  observed values are small numbers (0 or 1) when addressing a specific bind
  * @param elements  one entry per bound squad member slot
  */
final case class SquadBindInfoMessage(
    bindIndex: Int,
    elements: Vector[SquadBindEntry]
) extends PlanetSideGamePacket {
  type Packet = SquadBindInfoMessage
  def opcode = GamePacketOpcode.SquadBindInfoMessage
  def encode = SquadBindInfoMessage.encode(this)
}

object SquadBindInfoMessage extends Marshallable[SquadBindInfoMessage] {

  /** The bind state of a single squad member slot.
    *
    * @param slot    the squad member slot (position 0-9) this entry describes
    * @param zoneID  zone identifier, as in `zX` / `mapX`;
    *                `0` when the member is not bound
    * @param mapID   the "MapID" of the facility in `mapX.json` the member is bound to;
    *                for a member bound to an AMS this is likely the facility the AMS was deployed from;
    *                `0` when the member is not bound
    * @param isBound whether the member is bound;
    *                if `false`, `zoneID` and `mapID` are `0`
    */
  final case class SquadBindEntry(
    slot: Long,
    zoneID: Long,
    mapID: Int,
    isBound: Boolean
  )

  private implicit val squadBindEntryCodec: Codec[SquadBindEntry] = (
    ("slot" | uint32L) ::
      ("zoneID" | uint32L) ::
      ("mapID" | uint16L) ::
      ("isBound" | bool)
  ).as[SquadBindEntry]

  implicit val codec: Codec[SquadBindInfoMessage] = (
    ("bindIndex" | int32L) ::
      ("squadBindEntries" | vectorOfN(int32L, squadBindEntryCodec))
  ).as[SquadBindInfoMessage]
}
