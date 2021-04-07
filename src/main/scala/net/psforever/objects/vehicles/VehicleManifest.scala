// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.zones.Zone

/**
  * A record that records some passenger information.
  * @param name the passenger name for direct vehicle passengers;
  *             the driver name for cargo vehicles
  * @param mount the mount index
  */
final case class ManifestPassengerEntry(name: String, mount: Int)

/**
  * A record of accounting of the the vehicle's state at a given time.
  * @param file the id of this manifest entry;
  *             used as the channel name for summoning passengers to the vehicle
  *             after it has been loaded to a new location or to a new zone;
  *             this channel name should be unique to the vehicle for at least the duration of the transition;
  *             the vehicle-specific channel with which all passengers are coordinated back to the original vehicle
  * @param vehicle the vehicle in transport
  * @param origin where the vehicle originally was
  * @param driverName the name of the driver when the transport process started
  * @param passengers the paired names and mount indices of all passengers when the transport process started
  * @param cargo the paired driver names and cargo hold indices of all cargo vehicles when the transport process started
  */
final case class VehicleManifest(
    file: String,
    vehicle: Vehicle,
    origin: Zone,
    driverName: String,
    passengers: List[ManifestPassengerEntry],
    cargo: List[ManifestPassengerEntry]
)


object VehicleManifest {
  def apply(vehicle: Vehicle): VehicleManifest = {
    val driverName = vehicle.Seats(0).occupant match {
      case Some(driver) => driver.Name
      case None         => "MISSING_DRIVER"
    }
    val passengers = vehicle.Seats.toList
      .filter { case (index, mount) => index > 0 && mount.isOccupied }
      .map { case (index, mount) => ManifestPassengerEntry(mount.occupant.get.Name, index) }
    val cargo = vehicle.CargoHolds.toList
      .collect {
        case (index: Int, hold: Cargo) if hold.occupant.nonEmpty =>
          hold.occupant.get.Seats(0).occupant match {
            case Some(driver) => ManifestPassengerEntry(driver.Name, index)
            case None         => ManifestPassengerEntry("MISSING_DRIVER", index)
          }
      }
    VehicleManifest(ManifestChannelName(vehicle), vehicle, vehicle.Zone, driverName, passengers, cargo)
  }

  def ManifestChannelName(vehicle: Vehicle): String = {
    s"transport-vehicle-channel-${vehicle.GUID.guid}"
  }
}
