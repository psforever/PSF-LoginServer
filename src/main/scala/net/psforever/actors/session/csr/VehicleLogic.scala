// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, VehicleFunctions, VehicleOperations}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.{PlanetSideGameObject, Vehicle, Vehicles}
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles.control.BfrFlight
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.interaction.InteractsWithZone
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

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleVehicleState(pkt: VehicleStateMessage): Unit = {
    player.allowInteraction = false
    val VehicleStateMessage(
    vehicle_guid,
    unk1,
    pos,
    ang,
    vel,
    is_flying,
    unk6,
    unk7,
    wheels,
    is_decelerating,
    is_cloaked
    ) = pkt
    ops.GetVehicleAndSeat() match {
      case (Some(obj), Some(0)) =>
        //we're driving the vehicle
        sessionLogic.zoning.spawn.tryQueuedActivity(vel)
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
        CustomerServiceRepresentativeMode.topOffHealthOfPlayer(sessionLogic, player)
        CustomerServiceRepresentativeMode.topOffHealth(sessionLogic, obj)
        val (position, angle, velocity, notMountedState) = continent.GUID(obj.MountedIn) match {
          case Some(v: Vehicle) =>
            (pos, v.Orientation - Vector3.z(value = 90f) * Vehicles.CargoOrientation(obj).toFloat, v.Velocity, false)
          case _ =>
            (pos, ang, vel, true)
        }
        if (notMountedState) {
          sessionLogic.updateBlockMap(obj, position)
          if (obj.DeploymentState != DriveState.Deployed) {
            obj.Velocity = velocity
          } else {
            obj.Velocity = Some(Vector3.Zero)
          }
          if (obj.Definition.CanFly) {
            obj.Flying = is_flying //usually Some(7)
          }
          obj.Cloaked = obj.Definition.CanCloak && is_cloaked
        } else {
          obj.Velocity = None
          obj.Flying = None
        }
        player.Position = position //convenient
        obj.Position = position
        obj.Orientation = angle
        //
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            player.GUID,
            vehicle_guid,
            unk1,
            position,
            angle,
            velocity,
            obj.Flying,
            unk6,
            unk7,
            wheels,
            is_decelerating,
            obj.Cloaked
          )
        )
        sessionLogic.squad.updateSquad()
      case (None, _) =>
      //log.error(s"VehicleState: no vehicle $vehicle_guid found in zone")
      //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
      case (_, Some(index)) =>
        log.error(
          s"VehicleState: ${player.Name} should not be dispatching this kind of packet from vehicle ${vehicle_guid.guid} when not the driver (actually, seat $index)"
        )
      case _ => ()
    }
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
  }

  def handleFrameVehicleState(pkt: FrameVehicleStateMessage): Unit = {
    player.allowInteraction = false
    val FrameVehicleStateMessage(
    vehicle_guid,
    unk1,
    pos,
    ang,
    vel,
    unk2,
    unk3,
    unk4,
    is_crouched,
    is_airborne,
    ascending_flight,
    flight_time,
    unk9,
    unkA
    ) = pkt
    ops.GetVehicleAndSeat() match {
      case (Some(obj), Some(0)) =>
        //we're driving the vehicle
        sessionLogic.zoning.spawn.tryQueuedActivity(vel)
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
        CustomerServiceRepresentativeMode.topOffHealthOfPlayer(sessionLogic, player)
        CustomerServiceRepresentativeMode.topOffHealth(sessionLogic, obj)
        val (position, angle, velocity, notMountedState) = continent.GUID(obj.MountedIn) match {
          case Some(v: Vehicle) =>
            (pos, v.Orientation - Vector3.z(value = 90f) * Vehicles.CargoOrientation(obj).toFloat, v.Velocity, false)
          case _ =>
            (pos, ang, vel, true)
        }
        if (notMountedState) {
          sessionLogic.updateBlockMap(obj, position)
          if (obj.DeploymentState != DriveState.Kneeling) {
            obj.Velocity = velocity
            if (is_airborne) {
              val flight = if (ascending_flight) flight_time else -flight_time
              obj.Flying = Some(flight)
              obj.Actor ! BfrFlight.Soaring(flight)
            } else if (obj.Flying.nonEmpty) {
              obj.Flying = None
              obj.Actor ! BfrFlight.Landed
            }
          } else {
            obj.Velocity = None
            obj.Flying = None
          }
        } else {
          obj.Velocity = None
          obj.Flying = None
        }
        player.Position = position //convenient
        obj.Position = position
        obj.Orientation = angle
        obj.DeploymentState = if (is_crouched || !notMountedState) DriveState.Kneeling else DriveState.Mobile
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.FrameVehicleState(
            player.GUID,
            vehicle_guid,
            unk1,
            position,
            angle,
            velocity,
            unk2,
            unk3,
            unk4,
            is_crouched,
            is_airborne,
            ascending_flight,
            flight_time,
            unk9,
            unkA
          )
        )
        sessionLogic.squad.updateSquad()
      case (None, _) =>
      //log.error(s"VehicleState: no vehicle $vehicle_guid found in zone")
      //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
      case (_, Some(index)) =>
        log.error(
          s"VehicleState: ${player.Name} should not be dispatching this kind of packet from vehicle ${vehicle_guid.guid} when not the driver (actually, seat $index)"
        )
      case _ => ()
    }
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
  }

  def handleChildObjectState(pkt: ChildObjectStateMessage): Unit = {
    player.allowInteraction = false
    val ChildObjectStateMessage(object_guid, pitch, yaw) = pkt
    val (o, tools) = sessionLogic.shooting.FindContainedWeapon
    (o match {
      case Some(mount: Mountable) => (mount, mount.PassengerInSeat(player))
      case _                      => (None, None)
    }) match {
      case (None, _) | (_, None) => //error - we do not recognize being mounted or controlling anything, but what can we do about it?
        ()
      case (Some(_: Vehicle), Some(0)) => //see VSM or FVSM for valid cases
        ()
      case (Some(entity: PlanetSideGameObject with Mountable with InteractsWithZone), Some(_)) => //COSM is our primary upstream packet
        sessionLogic.zoning.spawn.tryQueuedActivity(player.Velocity)
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
        CustomerServiceRepresentativeMode.topOffHealthOfPlayer(sessionLogic, player)
        CustomerServiceRepresentativeMode.topOffHealth(sessionLogic, entity)
        sessionLogic.squad.updateSquad()
      case _ => //we can't disprove that COSM is our primary upstream packet, it's just that we may be missing some details
        sessionLogic.zoning.spawn.tryQueuedActivity(player.Velocity)
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
    }
    //in the following condition we are in control of the child
    tools.find(_.GUID == object_guid) match {
      case None =>
      //old warning; this state is problematic, but can trigger in otherwise valid instances
      //log.warn(
      //  s"ChildObjectState: ${player.Name} is using a different controllable agent than entity ${object_guid.guid}"
      //)
      case Some(tool) =>
        val angle = Vector3(0f, pitch, yaw)
        tool.Orientation = angle
        player.Orientation = angle
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.ChildObjectState(player.GUID, object_guid, pitch, yaw)
        )
    }
    //TODO status condition of "playing getting out of vehicle to allow for late packets without warning
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
  }

  def handleVehicleSubState(pkt: VehicleSubStateMessage): Unit = {
    val VehicleSubStateMessage(vehicle_guid, _, pos, ang, vel, unk1, _) = pkt
    sessionLogic.validObject(vehicle_guid, decorator = "VehicleSubState")
      .collect {
        case obj: Vehicle =>
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
      }
  }

  def handleDeployRequest(pkt: DeployRequestMessage): Unit = {
    val DeployRequestMessage(_, vehicle_guid, deploy_state, _, _, _) = pkt
    continent.GUID(vehicle_guid)
      .collect {
        case obj: Vehicle =>
          continent.Transport ! Zone.Vehicle.TryDeploymentChange(obj, deploy_state)
          obj
        case obj =>
          log.error(s"DeployRequest: ${player.Name} expected a vehicle, but found a ${obj.Definition.Name} instead")
          obj
      }
      .orElse {
        log.error(s"DeployRequest: ${player.Name} can not find entity $vehicle_guid")
        avatarActor ! AvatarActor.SetVehicle(None) //todo is this safe
        None
      }
  }

  /* messages */

  def handleCanDeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = {
    if (!Deployment.CheckForDeployState(state)) {
      CanNotChangeDeployment(obj, state, "incorrect deploy state")
    }
  }

  def handleCanUndeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = {
    if (!Deployment.CheckForUndeployState(state)) {
      CanNotChangeDeployment(obj, state, "incorrect undeploy state")
    }
  }

  def handleCanNotChangeDeployment(obj: Deployment.DeploymentObject, state: DriveState.Value, reason: String): Unit = {
    CanNotChangeDeployment(obj, state, reason)
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
    if (obj.DeploymentState != DriveState.Mobile) {
      obj.DeploymentState = DriveState.Mobile
      sendResponse(DeployRequestMessage(player.GUID, obj.GUID, DriveState.Mobile, 0, unk3=false, Vector3.Zero))
      continent.VehicleEvents ! VehicleServiceMessage(
        continent.id,
        VehicleAction.DeployRequest(player.GUID, obj.GUID, DriveState.Mobile, 0, unk2=false, Vector3.Zero)
      )
    }
  }
}
