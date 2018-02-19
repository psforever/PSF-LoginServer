// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.deploy

import akka.actor.Actor

/**
  * The logic governing `Deployment` objects that use the following messages:
  * `TryDeploymentChange`,
  * `TryDeploy`,
  * and `TryUndeploy`.
  * This is a mix-in trait for combining with existing `Receive` logic.
  * @see `Deployment`
  * @see `DriveState`
  */
trait DeploymentBehavior {
  this : Actor =>

  def DeploymentObject : Deployment.DeploymentObject

  val deployBehavior : Receive = {
    case Deployment.TryDeploymentChange(state) =>
      val obj = DeploymentObject
      if(Deployment.NextState(obj.DeploymentState) == state
        && (obj.DeploymentState = state) == state) {
        if(Deployment.CheckForDeployState(state)) {
          sender ! Deployment.CanDeploy(obj, state)
        }
        else { //may need to check in future
          sender ! Deployment.CanUndeploy(obj, state)
        }
      }
      else {
        sender ! Deployment.CanNotChangeDeployment(obj, state, "incorrect transition state")
      }

    case Deployment.TryDeploy(state) =>
      val obj = DeploymentObject
      if(Deployment.CheckForDeployState(state)
        && Deployment.NextState(obj.DeploymentState) == state
        && (obj.DeploymentState = state) == state) {
        sender ! Deployment.CanDeploy(obj, state)
      }
      else {
        sender ! Deployment.CanNotChangeDeployment(obj, state, "incorrect deploy transition state")
      }

    case Deployment.TryUndeploy(state) =>
      val obj = DeploymentObject
      if(Deployment.CheckForUndeployState(state)
        && Deployment.NextState(obj.DeploymentState) == state
        && (obj.DeploymentState = state) == state) {
        sender ! Deployment.CanUndeploy(obj, state)
      }
      else {
        sender ! Deployment.CanNotChangeDeployment(obj, state, "incorrect undeploy transition state")
      }
  }
}
