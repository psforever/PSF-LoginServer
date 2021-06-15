// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.DriveState

//ams
class AmsControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle) {

  override def specificResponseToDeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Deployed =>
        val zone  = vehicle.Zone
        val driverChannel = vehicle.Seats(0).occupant match {
          case Some(tplayer) => tplayer.Name
          case None          => ""
        }
        val events = zone.VehicleEvents
        events ! VehicleServiceMessage.AMSDeploymentChange(zone)
        events ! VehicleServiceMessage(driverChannel, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 81, 1))
      case _ => ;
    }
  }

  override def specificResponseToUndeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Undeploying =>
        val zone  = vehicle.Zone
        val driverChannel = vehicle.Seats(0).occupant match {
          case Some(tplayer) => tplayer.Name
          case None          => ""
        }
        val events = zone.VehicleEvents
        events ! VehicleServiceMessage.AMSDeploymentChange(zone)
        events ! VehicleServiceMessage(driverChannel, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vehicle.GUID, 81, 0))
      case _ => ;
    }
  }
}

