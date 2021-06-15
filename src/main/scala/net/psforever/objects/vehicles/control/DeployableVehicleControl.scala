// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.deploy.Deployment.DeploymentObject
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.types._

//switchblade
//flail
class DeployableVehicleControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
    with DeploymentBehavior {
  def DeploymentObject = vehicle

  override def commonEnabledBehavior : Receive = super.commonEnabledBehavior.orElse(deployBehavior)

  override def commonDisabledBehavior : Receive =
    super.commonDisabledBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
          deployBehavior.apply(msg)

        case msg @ Mountable.TryDismount(_, seat_num) =>
          dismountBehavior.apply(msg)
          dismountCleanup(seat_num)
      }

  override def commonDeleteBehavior : Receive =
    super.commonDeleteBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
          deployBehavior.apply(msg)
      }

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
    super.PrepareForDisabled(kickPassengers)
  }

  override def PrepareForDeletion() : Unit = {
    vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
    super.PrepareForDeletion()
  }

  override def TryDeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    DeployableVehicleControl.DeploymentAngleCheck(obj) && super.TryDeploymentChange(obj, state)
  }

  override def DeploymentAction(
                                 obj: DeploymentObject,
                                 state: DriveState.Value,
                                 prevState: DriveState.Value
                               ): DriveState.Value = {
    val out = super.DeploymentAction(obj, state, prevState)
    Vehicles.ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
    specificResponseToDeployment(state)
    out
  }

  def specificResponseToDeployment(state: DriveState.Value): Unit = { }

  override def UndeploymentAction(
                                   obj: DeploymentObject,
                                   state: DriveState.Value,
                                   prevState: DriveState.Value
                                 ): DriveState.Value = {
    val out = if (decaying) state else super.UndeploymentAction(obj, state, prevState)
    Vehicles.ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
    specificResponseToUndeployment(state)
    out
  }

  def specificResponseToUndeployment(state: DriveState.Value): Unit = { }
}

object DeployableVehicleControl {
  def DeploymentAngleCheck(obj: Deployment.DeploymentObject): Boolean = {
    obj.Orientation.x <= 30 || obj.Orientation.x >= 330
  }
}
