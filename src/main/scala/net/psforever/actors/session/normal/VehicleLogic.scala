// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, VehicleFunctions, VehicleOperations}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.{PlanetSideGameObject, Player, Vehicle, Vehicles}
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles.control.BfrFlight
import net.psforever.objects.zones.Zone
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
    GetVehicleAndSeat() match {
      case (Some(obj), Some(0)) =>
        //we're driving the vehicle
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
        sessionLogic.general.fallHeightTracker(pos.z)
        if (obj.MountedIn.isEmpty) {
          sessionLogic.updateBlockMap(obj, pos)
        }
        player.Position = pos //convenient
        if (obj.WeaponControlledFromSeat(0).isEmpty) {
          player.Orientation = Vector3.z(ang.z) //convenient
        }
        obj.Position = pos
        obj.Orientation = ang
        if (obj.MountedIn.isEmpty) {
          if (obj.DeploymentState != DriveState.Deployed) {
            obj.Velocity = vel
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
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            player.GUID,
            vehicle_guid,
            unk1,
            obj.Position,
            ang,
            obj.Velocity,
            if (obj.isFlying) {
              is_flying
            } else {
              None
            },
            unk6,
            unk7,
            wheels,
            is_decelerating,
            obj.Cloaked
          )
        )
        sessionLogic.squad.updateSquad()
        obj.zoneInteractions()
      case (None, _) =>
      //log.error(s"VehicleState: no vehicle $vehicle_guid found in zone")
      //TODO placing a "not driving" warning here may trigger as we are disembarking the vehicle
      case (_, Some(index)) =>
        log.error(
          s"VehicleState: ${player.Name} should not be dispatching this kind of packet from vehicle ${vehicle_guid.guid} when not the driver (actually, seat $index)"
        )
      case _ => ;
    }
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
  }

  def handleFrameVehicleState(pkt: FrameVehicleStateMessage): Unit = {
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
    GetVehicleAndSeat() match {
      case (Some(obj), Some(0)) =>
        //we're driving the vehicle
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
        val (position, angle, velocity, notMountedState) = continent.GUID(obj.MountedIn) match {
          case Some(v: Vehicle) =>
            sessionLogic.updateBlockMap(obj, pos)
            (pos, v.Orientation - Vector3.z(value = 90f) * Vehicles.CargoOrientation(obj).toFloat, v.Velocity, false)
          case _ =>
            (pos, ang, vel, true)
        }
        player.Position = position //convenient
        if (obj.WeaponControlledFromSeat(seatNumber = 0).isEmpty) {
          player.Orientation = Vector3.z(ang.z) //convenient
        }
        obj.Position = position
        obj.Orientation = angle
        obj.Velocity = velocity
        //            if (is_crouched && obj.DeploymentState != DriveState.Kneeling) {
        //              //dev stuff goes here
        //            }
        //            else
        //            if (!is_crouched && obj.DeploymentState == DriveState.Kneeling) {
        //              //dev stuff goes here
        //            }
        obj.DeploymentState = if (is_crouched || !notMountedState) DriveState.Kneeling else DriveState.Mobile
        if (notMountedState) {
          if (obj.DeploymentState != DriveState.Kneeling) {
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
          obj.zoneInteractions()
        } else {
          obj.Velocity = None
          obj.Flying = None
        }
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
      case _ => ;
    }
    if (player.death_by == -1) {
      sessionLogic.kickedByAdministration()
    }
  }

  def handleChildObjectState(pkt: ChildObjectStateMessage): Unit = {
    val ChildObjectStateMessage(object_guid, pitch, yaw) = pkt
    val (o, tools) = sessionLogic.shooting.FindContainedWeapon
    //is COSM our primary upstream packet?
    (o match {
      case Some(mount: Mountable) => (o, mount.PassengerInSeat(player))
      case _                      => (None, None)
    }) match {
      case (None, None) | (_, None) | (Some(_: Vehicle), Some(0)) => ;
      case _ =>
        sessionLogic.persist()
        sessionLogic.turnCounterFunc(player.GUID)
    }
    //the majority of the following check retrieves information to determine if we are in control of the child
    tools.find { _.GUID == object_guid } match {
      case None =>
      //todo: old warning; this state is problematic, but can trigger in otherwise valid instances
      //log.warn(
      //  s"ChildObjectState: ${player.Name} is using a different controllable agent than entity ${object_guid.guid}"
      //)
      case Some(_) =>
        //TODO set tool orientation?
        player.Orientation = Vector3(0f, pitch, yaw)
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

  def handleDeployRequest(pkt: DeployRequestMessage): Unit = {
    val DeployRequestMessage(_, vehicle_guid, deploy_state, _, _, _) = pkt
    val vehicle = player.avatar.vehicle
    if (vehicle.contains(vehicle_guid)) {
      if (vehicle == player.VehicleSeated) {
        continent.GUID(vehicle_guid) match {
          case Some(obj: Vehicle) =>
            log.info(s"${player.Name} is requesting a deployment change for ${obj.Definition.Name} - $deploy_state")
            obj.Actor ! Deployment.TryDeploymentChange(deploy_state)

          case _ =>
            log.error(s"DeployRequest: ${player.Name} can not find vehicle $vehicle_guid")
            avatarActor ! AvatarActor.SetVehicle(None)
        }
      } else {
        log.warn(s"${player.Name} must be mounted to request a deployment change")
      }
    } else {
      log.warn(s"DeployRequest: ${player.Name} does not own the deploying $vehicle_guid object")
    }
  }

  /* messages */

  def handleCanDeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = {
    if (state == DriveState.Deploying) {
      log.trace(s"DeployRequest: $obj transitioning to deploy state")
    } else if (state == DriveState.Deployed) {
      log.trace(s"DeployRequest: $obj has been Deployed")
    } else {
      CanNotChangeDeployment(obj, state, "incorrect deploy state")
    }
  }

  def handleCanUndeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit = {
    if (state == DriveState.Undeploying) {
      log.trace(s"DeployRequest: $obj transitioning to undeploy state")
    } else if (state == DriveState.Mobile) {
      log.trace(s"DeployRequest: $obj is Mobile")
    } else {
      CanNotChangeDeployment(obj, state, "incorrect undeploy state")
    }
  }

  def handleCanNotChangeDeployment(obj: Deployment.DeploymentObject, state: DriveState.Value, reason: String): Unit = {
    if (Deployment.CheckForDeployState(state) && !Deployment.AngleCheck(obj)) {
      CanNotChangeDeployment(obj, state, reason = "ground too steep")
    } else {
      CanNotChangeDeployment(obj, state, reason)
    }
  }

  /* support functions */

  /**
   * If the player is mounted in some entity, find that entity and get the mount index number at which the player is sat.
   * The priority of object confirmation is `direct` then `occupant.VehicleSeated`.
   * Once an object is found, the remainder are ignored.
   * @param direct a game object in which the player may be sat
   * @param occupant the player who is sat and may have specified the game object in which mounted
   * @return a tuple consisting of a vehicle reference and a mount index
   *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
   *         `(None, None)`, otherwise (even if the vehicle can be determined)
   */
  private def GetMountableAndSeat(
                                   direct: Option[PlanetSideGameObject with Mountable],
                                   occupant: Player,
                                   zone: Zone
                                 ): (Option[PlanetSideGameObject with Mountable], Option[Int]) =
    direct.orElse(zone.GUID(occupant.VehicleSeated)) match {
      case Some(obj: PlanetSideGameObject with Mountable) =>
        obj.PassengerInSeat(occupant) match {
          case index @ Some(_) =>
            (Some(obj), index)
          case None =>
            (None, None)
        }
      case _ =>
        (None, None)
    }

  /**
   * If the player is seated in a vehicle, find that vehicle and get the mount index number at which the player is sat.
   * @see `GetMountableAndSeat`
   * @return a tuple consisting of a vehicle reference and a mount index
   *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
   *         `(None, None)`, otherwise (even if the vehicle can be determined)
   */
  private def GetVehicleAndSeat(): (Option[Vehicle], Option[Int]) =
    GetMountableAndSeat(None, player, continent) match {
      case (Some(v: Vehicle), Some(seat)) => (Some(v), Some(seat))
      case _                              => (None, None)
    }

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
