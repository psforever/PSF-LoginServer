// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Note: For some reason this packet does not include the GUID of the vehicle that is being dismounted from. As a workaround vehicle.MountedIn in manually set/removed
  * @param player_guid // GUID of the player that is rqeuesting dismount
  * @param vehicle_guid GUID of the vehicle that is requesting dismount
  * @param bailed If the vehicle bailed out of the cargo vehicle
  * @param requestedByPassenger If a passenger of the vehicle in the cargo bay requests dismount this bit will be set
  * @param kicked If the vehicle was kicked by the cargo vehicle pilot
  */
final case class DismountVehicleCargoMsg(
    player_guid: PlanetSideGUID,
    vehicle_guid: PlanetSideGUID,
    bailed: Boolean,
    requestedByPassenger: Boolean,
    kicked: Boolean
) extends PlanetSideGamePacket {
  type Packet = DismountVehicleCargoMsg

  def opcode = GamePacketOpcode.DismountVehicleCargoMsg

  def encode = DismountVehicleCargoMsg.encode(this)
}

object DismountVehicleCargoMsg extends Marshallable[DismountVehicleCargoMsg] {
  implicit val codec: Codec[DismountVehicleCargoMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("unk3" | bool) :: // bailed?
      ("unk4" | bool) ::
      ("unk5" | bool)
  ).as[DismountVehicleCargoMsg]
}
