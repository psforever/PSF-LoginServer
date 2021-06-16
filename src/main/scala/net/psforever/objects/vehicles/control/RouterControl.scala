// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.types.DriveState

/**
  * A vehicle control agency exclusive to the router.
  * When deployed, any router telepad that was acquired from this particular router
  * and then constructed into a router telepad somewhere in the world
  * may synchronize with the vehicle to establish a short to medium range infantry teleportation system.
  * @param vehicle the router
  */
class RouterControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle) {

  /**
    * React to a deployment state change.
    * Activate the internal telepad mechanism.
    * @param state the deployment state
    */
  override def specificResponseToDeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Deployed =>
        vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) => util.Actor ! TelepadLike.Activate(util)
          case _ => ;
        }
      case _ => ;
    }
  }

  /**
    * React to an undeployment state change.
    * Deactivate the internal telepad mechanism.
    * @param state the deployment state
    */
  override def specificResponseToUndeployment(state: DriveState.Value): Unit = {
    state match {
      case DriveState.Undeploying =>
        vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) => util.Actor ! TelepadLike.Deactivate(util)
          case _ => ;
        }
      case _ => ;
    }
  }
}
