// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.vehicles.{Utility, UtilityType}
import net.psforever.types.DriveState

//router
class RouterControl(vehicle: Vehicle)
  extends DeployingVehicleControl(vehicle) {

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
