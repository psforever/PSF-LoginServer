// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.DriveState

/**
  * A vehicle control agency exclusive to the advanced mobile spawn (AMS).
  * When deployed, infantry troops may manifest nearby the vehicle
  * as they switch from being deconstructed (or dead) to being alive.
  * @param vehicle the AMS
  */
class AmsControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle) {

  /**
    * React to a deployment state change.
    * Announce that this AMS is ready to accept troop deployment.
    * @param state the deployment state
    */
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

  /**
    * React to an undeployment state change.
    * This AMS is now off the grid.
    * @param state the deployment state
    */
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

