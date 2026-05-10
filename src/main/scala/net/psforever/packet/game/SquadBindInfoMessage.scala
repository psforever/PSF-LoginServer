// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.SquadBindInfoMessage.SquadBindEntry
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SquadBindInfoMessage(
    unk0: Int, // squad/platoon index?
    elements: Vector[SquadBindEntry],
  ) extends PlanetSideGamePacket {
  type Packet = SquadBindInfoMessage
  def opcode = GamePacketOpcode.SquadBindInfoMessage
  def encode = SquadBindInfoMessage.encode(this)
}

object SquadBindInfoMessage extends Marshallable[SquadBindInfoMessage] {

  /**
    * SquadBindEntry
    *
    * If isBound is false unk1 and unk2 are 0.
    * unk1
    * @param squadMember index of squad member
    * @param zoneID zone ID as in zX / mapX
    * @param mapID MapID identifier in mapX.json
    * @param isBound is bound to a facility
    */
  final case class SquadBindEntry(
    squadMember: Long,
    zoneID: Long,
    mapID: Int,
    isBound: Boolean,
  )

  private implicit val squadBindEntryCodec: Codec[SquadBindEntry] = (
    ("squadMember" | uint32L) ::
      ("zoneID" | uint32L) ::
      ("mapID" | uint16L) ::
      ("isBound" | bool)
    ).as[SquadBindEntry]

  implicit val codec: Codec[SquadBindInfoMessage] = (
    ("unk0" | int32L) ::
      ("squadBindEntries" | vectorOfN(int32L, squadBindEntryCodec))
    ).as[SquadBindInfoMessage]
}
