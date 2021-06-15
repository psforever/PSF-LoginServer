// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.vehicles.CargoBehavior

//dropship (galaxy)
//lodestar
class CargoCarrierControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
    with CargoBehavior {
  def CargoObject = vehicle

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior.orElse(cargoBehavior)

  override def PrepareForDisabled(kickPassengers : Boolean) : Unit = {
    //abandon all cargo
    vehicle.CargoHolds.values
      .collect {
        case hold if hold.isOccupied =>
          val cargo = hold.occupant.get
          CargoBehavior.HandleVehicleCargoDismount(
            cargo.GUID,
            cargo,
            vehicle.GUID,
            vehicle,
            bailed = false,
            requestedByPassenger = false,
            kicked = false
          )
      }
    super.PrepareForDisabled(kickPassengers)
  }
}
