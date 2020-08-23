// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{CargoStatus, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @see `CargoStatus`
  * @see `MountVehicleCargoMsg`
  * @param cargo_vehicle_guid always the carrier vehicle
  * @param requesting_vehicle cargo vehicle that requested mounting during the mounting process;
  *                           blank when the process is complete
  * @param mounted_vehicle    cargo vehicle that requested mounting after the mounting process is complete;
  *                           blank before the process is complete
  * @param dismounted_vehicle cargo vehicle that was mounted after disembarking;
  *                           blank before disembark
  *                           blank before mounting request, when packet `MountVehicleCargoMsg` is received
  * @param slot               cargo hold mount position;
  *                           common values are 1 for the `lodestar` and 15 for the `dropship`
  * @param mount_status       cargo mount status
  * @param orientation        direction the cargo vehicle faces when stowed in the carrier cargo bay;
  *                           0 is "normal," front facing forward;
  *                           1 is "sideways," front facing the side of the carrier vehicle, e.g. `router`
  */
final case class CargoMountPointStatusMessage(
    cargo_vehicle_guid: PlanetSideGUID,
    requesting_vehicle: PlanetSideGUID,
    mounted_vehicle: PlanetSideGUID,
    dismounted_vehicle: PlanetSideGUID,
    slot: Int,
    mount_status: CargoStatus.Value,
    orientation: Int
) extends PlanetSideGamePacket {
  type Packet = CargoMountPointStatusMessage

  def opcode = GamePacketOpcode.CargoMountPointStatusMessage

  def encode = CargoMountPointStatusMessage.encode(this)
}

object CargoMountPointStatusMessage extends Marshallable[CargoMountPointStatusMessage] {
  implicit val codec: Codec[CargoMountPointStatusMessage] = (
    ("cargo_vehicle_guid" | PlanetSideGUID.codec) ::
      ("requesting_vehicle" | PlanetSideGUID.codec) ::
      ("mounted_vehicle" | PlanetSideGUID.codec) ::
      ("dismounted_vehicle" | PlanetSideGUID.codec) ::
      ("slot" | uint8L) ::
      ("mount_status" | CargoStatus.codec) ::
      ("orientation" | uint2L)
  ).as[CargoMountPointStatusMessage]
}
