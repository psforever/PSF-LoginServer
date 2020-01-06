// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  *
  * @param player_guid The guid of the player sending the request to board another vehicle with a cargo vehicle
  * @param vehicle_guid The guid of the vehicle for the requesting player
  * @param target_vehicle The cargo vehicle guid e.g. Galaxy / Lodestar
  * @param unk4
  */
final case class MountVehicleCargoMsg(player_guid : PlanetSideGUID, vehicle_guid: PlanetSideGUID, target_vehicle: PlanetSideGUID, unk4: Int)
  extends PlanetSideGamePacket {
  type Packet = MountVehicleCargoMsg

  def opcode = GamePacketOpcode.MountVehicleCargoMsg

  def encode = MountVehicleCargoMsg.encode(this)
}

object MountVehicleCargoMsg extends Marshallable[MountVehicleCargoMsg] {
  implicit val codec : Codec[MountVehicleCargoMsg] = (
      ("unk1" | PlanetSideGUID.codec) ::
        ("unk2" | PlanetSideGUID.codec)::
        ("unk3" | PlanetSideGUID.codec) ::
        ("unk4" | uint8L)
    ).as[MountVehicleCargoMsg]
}
