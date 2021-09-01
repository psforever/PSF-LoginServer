// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Request dismount of one vehicle (cargo) that is being ferried by another vehicle (carrier).
  * The carrier has what is called a "cargo bay" which is where the cargo is being stored for ferrying.
  * @param player_guid GUID of the player that is rqeuesting dismount;
  *                    when kicked by carrier driver, player_guid will be PlanetSideGUID(0);
  *                    when exiting of the cargo vehicle driver's own accord, player_guid will be the cargo vehicle driver
  * @param vehicle_guid GUID of the vehicle that is requesting dismount (cargo)
  * @param bailed if the cargo vehicle bailed out of the cargo vehicle
  * @param requestedByPassenger if a passenger of the cargo vehicle requests dismount
  * @param kicked if the cargo vehicle was kicked by the cargo vehicle pilot
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
