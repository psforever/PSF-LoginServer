// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.SquadBindInfoMessage.SquadBindEntry
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SquadBindInfoMessage(
                                       unk0: Int, // squad?
                                       elements: Vector[SquadBindEntry],
  ) extends PlanetSideGamePacket {
  type Packet = SquadBindInfoMessage
  def opcode = GamePacketOpcode.SquadBindInfoMessage
  def encode = SquadBindInfoMessage.encode(this)
}

object SquadBindInfoMessage extends Marshallable[SquadBindInfoMessage] {

  final case class SquadBindEntry(
    unk0: Long,
    unk1: Long,
    unk2: Int,
    unk3: Boolean,
  )


  private implicit val squadBindEntryCodec: Codec[SquadBindEntry] = (
    ("unk0" | uint32L) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint16L) ::
      ("unk3" | bool)
    ).as[SquadBindEntry]

  implicit val codec: Codec[SquadBindInfoMessage] = (
    ("unk0" | int32L) ::
      ("squadBindEntries" | vectorOfN(int32L, squadBindEntryCodec))
    ).as[SquadBindInfoMessage]
}
