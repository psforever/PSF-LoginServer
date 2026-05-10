// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * SquadFacilityBindInfoMessage
  * @param unk0
  * @param squadID ID of the squad this message is relating to
  * @param mapID MapID of the facility the player is bound to;
  *              alternatively of the facility the AMS was pulled from?;
  * @param zoneID ZoneID of where the bind is from and the MapID relates to
  */
final case class SquadFacilityBindInfoMessage(
    unk0: Boolean,
    squadID: Long,
    mapID: Long,
    zoneID: Long
  ) extends PlanetSideGamePacket {
  type Packet = EmpireBenefitsMessage
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
