// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects._
import net.psforever.objects.serverobject.deploy.Deployment.DeploymentObject
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.types._

/**
  * A vehicle control agency exclusive to vehicles that can switch out a navigation mode
  * and convert to a sessile mode that affords additional functionality.
  * This includes only the Switchblade and the Flail.
  * Other vehicles that deploy are handled by specific instances of this control agency.
  * @see `AmsControl`
  * @see `AntControl`
  * @see `RouterControl`
  * @param vehicle the vehicle
  */
class DeployingVehicleControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
    with DeploymentBehavior {
  def DeploymentObject = vehicle

  override def commonEnabledBehavior : Receive = super.commonEnabledBehavior.orElse(deployBehavior)

  /**
    * Even when disabled, the vehicle can be made to undeploy.
    * Even when disabled, passengers can formally dismount from the vehicle.
    */
  override def commonDisabledBehavior : Receive =
    super.commonDisabledBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
          deployBehavior.apply(msg)

        case msg @ Mountable.TryDismount(_, seat_num) =>
          dismountBehavior.apply(msg)
          dismountCleanup(seat_num)
      }

  /**
    * Even when on the verge of deletion, the vehicle can be made to undeploy.
    */
  override def commonDeleteBehavior : Receive =
    super.commonDeleteBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
          deployBehavior.apply(msg)
      }

  /**
    * Even when disabled, the vehicle can be made to undeploy.
    */
  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
    super.PrepareForDisabled(kickPassengers)
  }

  /**
    * Even when on the verge of deletion, the vehicle can be made to undeploy.
    */
  override def PrepareForDeletion() : Unit = {
    vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
    super.PrepareForDeletion()
  }

  override def TryDeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    Deployment.AngleCheck(obj) && super.TryDeploymentChange(obj, state)
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
