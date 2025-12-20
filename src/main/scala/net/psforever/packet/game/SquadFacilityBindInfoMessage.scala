// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SquadFacilityBindInfoMessage(
    unk0: Boolean,
    unk1: Long,
    unk2: Long,
    unk3: Long
  ) extends PlanetSideGamePacket {
  type Packet = EmpireBenefitsMessage
  def opcode = GamePacketOpcode.SquadFacilityBindInfoMessage
  def encode = SquadFacilityBindInfoMessage.encode(this)
}

object SquadFacilityBindInfoMessage extends Marshallable[SquadFacilityBindInfoMessage] {

  implicit val codec: Codec[SquadFacilityBindInfoMessage] = (
    ("unk0" | bool) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L)
    ).as[SquadFacilityBindInfoMessage]
}
