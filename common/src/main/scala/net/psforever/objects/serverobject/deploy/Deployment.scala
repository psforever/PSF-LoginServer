// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.deploy

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.DriveState

/**
  * A `trait` for the purposes of deploying a server object that supports the operation.
  * As a mixin, it provides the local variable used to keep track of the deployment state
  * and the logic to change the value of the deployment.
  * Initially, the deployment state is `Mobile`.
  */
trait Deployment {
  this: PlanetSideServerObject =>

  private var deployState: DriveState.Value = DriveState.Mobile

  def DeployTime: Int = 0 //ms

  def UndeployTime: Int = 0 //ms

  def DeploymentState: DriveState.Value = deployState

  def DeploymentState_=(to_deploy_state: DriveState.Value): DriveState.Value = {
    deployState = to_deploy_state
    DeploymentState
  }
}

object Deployment {

  /**
    * A shorthand `type` for a valid object of `Deployment`.
    */
  type DeploymentObject = PlanetSideServerObject with Deployment

  /**
    * A message for instigating a change in deployment state.
    * @param state the new deployment state
    */
  final case class TryDeploymentChange(state: DriveState.Value)

  /**
    * A message for instigating a change to a deploy state.
    * @param state the new deploy state
    */
  final case class TryDeploy(state: DriveState.Value)

  /**
    * A message for instigating a change to an undeploy state.
    * @param state the new undeploy state
    */
  final case class TryUndeploy(state: DriveState.Value)

  /**
    * A response message to report successful deploy change.
    * @param obj the object being deployed
    * @param state the new deploy state
    */
  final case class CanDeploy(obj: DeploymentObject, state: DriveState.Value)

  /**
    * A response message to report successful undeploy change.
    * @param obj the object being undeployed
    * @param state the new undeploy state
    */
  final case class CanUndeploy(obj: DeploymentObject, state: DriveState.Value)

  /**
    * A response message to report an unsuccessful deployment change.
    * @param obj the object being changed
    * @param to_state the attempted deployment state
    * @param reason a string explaining why the state can not or will not change
    */
  final case class CanNotChangeDeployment(obj: DeploymentObject, to_state: DriveState.Value, reason: String)

  /**
    * Given a starting deployment state, provide the next deployment state in a sequence.<br>
    * <br>
    * Two sequences are defined.
    * The more elaborate sequence proceeds from `Mobile --> Deploying --> Deployed --> Undeploying --> Mobile`.
    * This is the standard in-game deploy cycle.
    * The sequence void of complexity is `State7 --> State7`.
    * `State7` is an odd condition possessed mainly by vehicles that do not deploy.
    * @param from_state the original deployment state
    * @return the deployment state that is being transitioned
    */
  def NextState(from_state: DriveState.Value): DriveState.Value = {
    from_state match {
      case DriveState.Mobile      => DriveState.Deploying
      case DriveState.Deploying   => DriveState.Deployed
      case DriveState.Deployed    => DriveState.Undeploying
      case DriveState.Undeploying => DriveState.Mobile
      case DriveState.State7      => DriveState.State7
    }
  }

  /**
    * Is this `state` considered one of "deploy?"
    * @param state the state to check
    * @return yes, if it is a valid state; otherwise, false
    */
  def CheckForDeployState(state: DriveState.Value): Boolean =
    state == DriveState.Deploying || state == DriveState.Deployed

  /**
    * Is this `state` considered one of "undeploy?"
    * @param state the state to check
    * @return yes, if it is a valid state; otherwise, false
    */
  def CheckForUndeployState(state: DriveState.Value): Boolean =
    state == DriveState.Undeploying || state == DriveState.Mobile || state == DriveState.State7
}
