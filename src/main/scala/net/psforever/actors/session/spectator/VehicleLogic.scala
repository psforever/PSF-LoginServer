// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, VehicleFunctions, VehicleOperations}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.packet.game.{ChildObjectStateMessage, DeployRequestMessage, FrameVehicleStateMessage, VehicleStateMessage, VehicleSubStateMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{DriveState, Vector3}

object VehicleLogic {
  def apply(ops: VehicleOperations): VehicleLogic = {
    new VehicleLogic(ops, ops.context)
  }
}

class VehicleLogic(val ops: VehicleOperations, implicit val context: ActorContext) extends VehicleFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  //private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleVehicleState(pkt: VehicleStateMessage): Unit = { /* can not drive vehicle as spectator */ }

  def handleFrameVehicleState(pkt: FrameVehicleStateMessage): Unit = { /* can not drive vehicle as spectator */ }

  def handleChildObjectState(pkt: ChildObjectStateMessage): Unit = { /* can not drive vehicle as spectator */ }

  def handleVehicleSubState(pkt: VehicleSubStateMessage): Unit = {
    val VehicleSubStateMessage(vehicle_guid, _, pos, ang, vel, unk1, _) = pkt
    sessionLogic.validObject(vehicle_guid, decorator = "VehicleSubState") match {
      case Some(obj: Vehicle) =>
        import net.psforever.login.WorldSession.boolToInt
        obj.Position = pos
        obj.Orientation = ang
        obj.Velocity = vel
        sessionLogic.updateBlockMap(obj, pos)
        obj.zoneInteractions()
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            player.GUID,
            vehicle_guid,
            unk1,
            pos,
            ang,
            obj.Velocity,
            obj.Flying,
            0,
            0,
            15,
            unk5 = false,
            obj.Cloaked
          )
        )
      case _ => ()
    }
  }

  def handleDeployRequest(pkt: DeployRequestMessage): Unit = { /* can not drive vehicle as spectator */ }

  /* messages */

  def handleCanDeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = { /* intentionally blank */ }

  def handleCanUndeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = { /* intentionally blank */ }

  def handleCanNotChangeDeployment(obj: Deployment.DeploymentObject, state: DriveState.Value, reason: String): Unit = {
    if (Deployment.CheckForDeployState(state) && !Deployment.AngleCheck(obj)) {
      CanNotChangeDeployment(obj, state, reason = "ground too steep")
    } else {
      CanNotChangeDeployment(obj, state, reason)
    }
  }

  /* support functions */

  /**
   * Common reporting behavior when a `Deployment` object fails to properly transition between states.
   * @param obj the game object that could not
   * @param state the `DriveState` that could not be promoted
   * @param reason a string explaining why the state can not or will not change
   */
  private def CanNotChangeDeployment(
                                      obj: PlanetSideServerObject with Deployment,
                                      state: DriveState.Value,
                                      reason: String
                                    ): Unit = {
    val mobileShift: String = if (obj.DeploymentState != DriveState.Mobile) {
      obj.DeploymentState = DriveState.Mobile
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Mobile, 0, unk3=false, Vector3.Zero))
      continent.VehicleEvents ! VehicleServiceMessage(
        continent.id,
        VehicleAction.DeployRequest(player.GUID, obj.GUID, DriveState.Mobile, 0, unk2=false, Vector3.Zero)
      )
      "; enforcing Mobile deployment state"
    } else {
      ""
    }
    log.error(s"DeployRequest: ${player.Name} can not transition $obj to $state - $reason$mobileShift")
  }
}
