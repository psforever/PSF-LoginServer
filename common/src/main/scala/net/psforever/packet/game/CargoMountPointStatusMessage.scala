// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.CargoStatus
import scodec.Codec
import scodec.codecs._

/**
  *
  * @param cargo_vehicle_guid Cargo vehicle GUID (galaxy / lodestar)
  * @param requesting_vehicle Seems to be vehicle that requested mounting (0 after mount)
  * @param mounted_vehicle Seems to be vehicle that requested mounting after mount (0 before mount)
  * @param dismounted_vehicle Seems to be vehicle that was mounted after disembarking (0 before embark or reset to 0 when MountVehicleCargoMsg received)
  * @param slot Mount point for cargo bay 1 = lodestar, 15 = galaxy
  * @param mount_status Mount status? 0 = None, 1 = Mount/Dismount in progress, 3 = Mounted
  * @param orientation 0 = normal, 1 = sideways (e.g. router in lodestar)
  */
final case class CargoMountPointStatusMessage(cargo_vehicle_guid : PlanetSideGUID,
                                              requesting_vehicle: PlanetSideGUID,
                                              mounted_vehicle: PlanetSideGUID,
                                              dismounted_vehicle: PlanetSideGUID,
                                              slot: Int,
                                              mount_status: CargoStatus.Value,
                                              orientation: Int)
  extends PlanetSideGamePacket {
  type Packet = CargoMountPointStatusMessage

  def opcode = GamePacketOpcode.CargoMountPointStatusMessage

  def encode = CargoMountPointStatusMessage.encode(this)
}

object CargoMountPointStatusMessage extends Marshallable[CargoMountPointStatusMessage] {
  implicit val codec : Codec[CargoMountPointStatusMessage] = (
    ("cargo_vehicle_guid" | PlanetSideGUID.codec) ::
    ("requesting_vehicle" | PlanetSideGUID.codec) ::
    ("mounted_vehicle" | PlanetSideGUID.codec) ::
    ("dismounted_vehicle" | PlanetSideGUID.codec) ::
    ("slot" | uint8L) ::
    ("mount_status" | CargoStatus.codec) ::
    ("orientation" | uint2L)
    ).as[CargoMountPointStatusMessage]
}

