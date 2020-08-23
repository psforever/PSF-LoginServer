// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.deploy

import akka.actor.Actor
import net.psforever.types.{DriveState, Vector3}
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * The logic governing `Deployment` objects that use the following messages:
  * `TryDeploymentChange`,
  * `TryDeploy`,
  * and `TryUndeploy`.
  * This is a mix-in trait for combining with existing `Receive` logic.
  *
  * @see `Deployment`
  * @see `DriveState`
  */
trait DeploymentBehavior {
  _: Actor =>

  def DeploymentObject: Deployment.DeploymentObject

  val deployBehavior: Receive = {
    case Deployment.TryDeploymentChange(state) =>
      sender() ! TryDeploymentStateChange(state)

    case Deployment.TryDeploy(state) =>
      sender() ! TryDeployStateChange(state)

    case Deployment.TryUndeploy(state) =>
      sender() ! TryUndeployStateChange(state)
  }

  def TryDeploymentStateChange(state: DriveState.Value): Any = {
    val obj       = DeploymentObject
    val prevState = obj.DeploymentState
    if (TryDeploymentChange(obj, state)) {
      if (Deployment.CheckForDeployState(state)) {
        DeploymentAction(obj, state, prevState)
        Deployment.CanDeploy(obj, state)
      } else {
        UndeploymentAction(obj, state, prevState)
        Deployment.CanUndeploy(obj, state)
      }
    } else {
      Deployment.CanNotChangeDeployment(obj, state, "incorrect transition state")
    }
  }

  def TryDeployStateChange(state: DriveState.Value): Any = {
    val obj       = DeploymentObject
    val prevState = obj.DeploymentState
    if (Deployment.CheckForDeployState(state) && TryDeploymentChange(obj, state)) {
      DeploymentAction(obj, state, prevState)
      Deployment.CanDeploy(obj, state)
    } else {
      Deployment.CanNotChangeDeployment(obj, state, "incorrect deploy transition state")
    }
  }

  def TryUndeployStateChange(state: DriveState.Value): Any = {
    val obj       = DeploymentObject
    val prevState = obj.DeploymentState
    if (Deployment.CheckForUndeployState(state) && TryUndeploymentChange(obj, state)) {
      UndeploymentAction(obj, state, prevState)
      Deployment.CanUndeploy(obj, state)
    } else {
      Deployment.CanNotChangeDeployment(obj, state, "incorrect undeploy transition state")
    }
  }

  def TryDeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    DeploymentBehavior.TryDeploymentChange(obj, state)
  }

  def TryUndeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    DeploymentBehavior.TryDeploymentChange(obj, state)
  }

  def DeploymentAction(
      obj: Deployment.DeploymentObject,
      state: DriveState.Value,
      prevState: DriveState.Value
  ): DriveState.Value = {
    val guid        = obj.GUID
    val zone        = obj.Zone
    val zoneChannel = zone.id
    val GUID0       = Service.defaultPlayerGUID
    //TODO remove this arbitrary allowance angle when no longer helpful
    if (obj.Orientation.x > 30 && obj.Orientation.x < 330) {
      obj.DeploymentState = prevState
      prevState
    } else if (state == DriveState.Deploying) {
      obj.Velocity = Some(Vector3.Zero) //no velocity
      zone.VehicleEvents ! VehicleServiceMessage(
        zoneChannel,
        VehicleAction.DeployRequest(GUID0, guid, state, 0, false, Vector3.Zero)
      )
      context.system.scheduler.scheduleOnce(
        obj.DeployTime milliseconds,
        obj.Actor,
        Deployment.TryDeploy(DriveState.Deployed)
      )
      state
    } else if (state == DriveState.Deployed) {
      obj.Velocity = Some(Vector3.Zero) //no velocity
      zone.VehicleEvents ! VehicleServiceMessage(
        zoneChannel,
        VehicleAction.DeployRequest(GUID0, guid, state, 0, false, Vector3.Zero)
      )
      state
    } else {
      prevState
    }
  }

  def UndeploymentAction(
      obj: Deployment.DeploymentObject,
      state: DriveState.Value,
      prevState: DriveState.Value
  ): DriveState.Value = {
    val guid        = obj.GUID
    val zone        = obj.Zone
    val zoneChannel = zone.id
    val GUID0       = Service.defaultPlayerGUID
    if (state == DriveState.Undeploying) {
      zone.VehicleEvents ! VehicleServiceMessage(
        zoneChannel,
        VehicleAction.DeployRequest(GUID0, guid, state, 0, false, Vector3.Zero)
      )
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(
        obj.UndeployTime milliseconds,
        obj.Actor,
        Deployment.TryUndeploy(DriveState.Mobile)
      )
      state
    } else if (state == DriveState.Mobile) {
      zone.VehicleEvents ! VehicleServiceMessage(
        zoneChannel,
        VehicleAction.DeployRequest(GUID0, guid, state, 0, false, Vector3.Zero)
      )
      state
    } else {
      prevState
    }
  }
}

object DeploymentBehavior {
  def TryDeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    Deployment.NextState(obj.DeploymentState) == state && (obj.DeploymentState = state) == state
  }
}
