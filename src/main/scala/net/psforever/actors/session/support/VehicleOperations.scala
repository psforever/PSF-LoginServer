// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles.control.BfrFlight
import net.psforever.objects.vehicles.{AccessPermissionGroup, CargoBehavior}
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game.{ChildObjectStateMessage, DeployRequestMessage, DismountVehicleCargoMsg, DismountVehicleMsg, MountVehicleCargoMsg, MountVehicleMsg, VehicleSubStateMessage, _}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, DriveState, Vector3}

class VehicleOperations(
                         val sessionData: SessionData,
                         avatarActor: typed.ActorRef[AvatarActor.Command],
                         implicit val context: ActorContext
                       ) extends CommonSessionInterfacingFunctionality {
  private[support] var serverVehicleControlVelocity: Option[Int] = None

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
        sessionData.persist()
        sessionData.turnCounterFunc(player.GUID)
        sessionData.fallHeightTracker(pos.z)
        if (obj.MountedIn.isEmpty) {
          sessionData.updateBlockMap(obj, pos)
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
        sessionData.squad.updateSquad()
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
      sessionData.kickedByAdministration()
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
        sessionData.persist()
        sessionData.turnCounterFunc(player.GUID)
        val (position, angle, velocity, notMountedState) = continent.GUID(obj.MountedIn) match {
          case Some(v: Vehicle) =>
            sessionData.updateBlockMap(obj, pos)
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
        sessionData.squad.updateSquad()
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
      sessionData.kickedByAdministration()
    }
  }

  def handleChildObjectState(pkt: ChildObjectStateMessage): Unit = {
    val ChildObjectStateMessage(object_guid, pitch, yaw) = pkt
    val (o, tools) = sessionData.shooting.FindContainedWeapon
    //is COSM our primary upstream packet?
    (o match {
      case Some(mount: Mountable) => (o, mount.PassengerInSeat(player))
      case _                      => (None, None)
    }) match {
      case (None, None) | (_, None) | (Some(_: Vehicle), Some(0)) => ;
      case _ =>
        sessionData.persist()
        sessionData.turnCounterFunc(player.GUID)
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
      sessionData.kickedByAdministration()
    }
  }

  def handleVehicleSubState(pkt: VehicleSubStateMessage): Unit = {
    val VehicleSubStateMessage(vehicle_guid, _, pos, ang, vel, unk1, _) = pkt
    sessionData.validObject(vehicle_guid, decorator = "VehicleSubState") match {
      case Some(obj: Vehicle) =>
        import net.psforever.login.WorldSession.boolToInt
        obj.Position = pos
        obj.Orientation = ang
        obj.Velocity = vel
        sessionData.updateBlockMap(obj, pos)
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
      case _ => ;
    }
  }

  def handleMountVehicle(pkt: MountVehicleMsg): Unit = {
    val MountVehicleMsg(_, mountable_guid, entry_point) = pkt
    sessionData.validObject(mountable_guid, decorator = "MountVehicle").collect {
      case obj: Mountable =>
        obj.Actor ! Mountable.TryMount(player, entry_point)
      case _ =>
        log.error(s"MountVehicleMsg: object ${mountable_guid.guid} not a mountable thing, ${player.Name}")
    }
  }

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit = {
    val DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) = pkt
    val dError: (String, Player)=> Unit = dismountError(bailType, wasKickedByDriver)
    //TODO optimize this later
    //common warning for this section
    if (player.GUID == player_guid) {
      //normally disembarking from a mount
      (sessionData.zoning.interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
        case out @ Some(obj: Vehicle) =>
          continent.GUID(obj.MountedIn) match {
            case Some(_: Vehicle) => None //cargo vehicle
            case _                => out  //arrangement "may" be permissible
          }
        case out @ Some(_: Mountable) =>
          out
        case _ =>
          dError(s"DismountVehicleMsg: player ${player.Name} not considered seated in a mountable entity", player)
          None
      }) match {
        case Some(obj: Mountable) =>
          obj.PassengerInSeat(player) match {
            case Some(seat_num) =>
              obj.Actor ! Mountable.TryDismount(player, seat_num, bailType)
              //short-circuit the temporary channel for transferring between zones, the player is no longer doing that
              sessionData.zoning.interstellarFerry = None
              // Deconstruct the vehicle if the driver has bailed out and the vehicle is capable of flight
              //todo: implement auto landing procedure if the pilot bails but passengers are still present instead of deconstructing the vehicle
              //todo: continue flight path until aircraft crashes if no passengers present (or no passenger seats), then deconstruct.
              //todo: kick cargo passengers out. To be added after PR #216 is merged
              obj match {
                case v: Vehicle
                  if bailType == BailType.Bailed &&
                    v.SeatPermissionGroup(seat_num).contains(AccessPermissionGroup.Driver) &&
                    v.isFlying =>
                  v.Actor ! Vehicle.Deconstruct(None) //immediate deconstruction
                case _ => ;
              }

            case None =>
              dError(s"DismountVehicleMsg: can not find where player ${player.Name}_guid is seated in mountable ${player.VehicleSeated}", player)
          }
        case _ =>
          dError(s"DismountVehicleMsg: can not find mountable entity ${player.VehicleSeated}", player)
      }
    } else {
      //kicking someone else out of a mount; need to own that mount/mountable
      val dWarn: (String, Player)=> Unit = dismountWarning(bailType, wasKickedByDriver)
      player.avatar.vehicle match {
        case Some(obj_guid) =>
          (
            (
              sessionData.validObject(obj_guid, decorator = "DismountVehicle/Vehicle"),
              sessionData.validObject(player_guid, decorator = "DismountVehicle/Player")
            ) match {
              case (vehicle @ Some(obj: Vehicle), tplayer) =>
                if (obj.MountedIn.isEmpty) (vehicle, tplayer) else (None, None)
              case (mount @ Some(_: Mountable), tplayer) =>
                (mount, tplayer)
              case _ =>
                (None, None)
            }) match {
            case (Some(obj: Mountable), Some(tplayer: Player)) =>
              obj.PassengerInSeat(tplayer) match {
                case Some(seat_num) =>
                  obj.Actor ! Mountable.TryDismount(tplayer, seat_num, bailType)
                case None =>
                  dError(s"DismountVehicleMsg: can not find where other player ${tplayer.Name} is seated in mountable $obj_guid", tplayer)
              }
            case (None, _) =>
              dWarn(s"DismountVehicleMsg: ${player.Name} can not find his vehicle", player)
            case (_, None) =>
              dWarn(s"DismountVehicleMsg: player $player_guid could not be found to kick, ${player.Name}", player)
            case _ =>
              dWarn(s"DismountVehicleMsg: object is either not a Mountable or not a Player", player)
          }
        case None =>
          dWarn(s"DismountVehicleMsg: ${player.Name} does not own a vehicle", player)
      }
    }
  }

  private def dismountWarning(
                               bailAs: BailType.Value,
                               kickedByDriver: Boolean
                             )
                             (
                               note: String,
                               player: Player
                             ): Unit = {
    log.warn(note)
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  private def dismountError(
                             bailAs: BailType.Value,
                             kickedByDriver: Boolean
                           )
                           (
                             note: String,
                             player: Player
                           ): Unit = {
    log.error(s"$note; some vehicle might not know that ${player.Name} is no longer sitting in it")
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = {
    val MountVehicleCargoMsg(_, cargo_guid, carrier_guid, _) = pkt
    (continent.GUID(cargo_guid), continent.GUID(carrier_guid)) match {
      case (Some(cargo: Vehicle), Some(carrier: Vehicle)) =>
        carrier.CargoHolds.find({ case (_, hold) => !hold.isOccupied }) match {
          case Some((mountPoint, _)) =>
            cargo.Actor ! CargoBehavior.StartCargoMounting(carrier_guid, mountPoint)
          case _ =>
            log.warn(
              s"MountVehicleCargoMsg: ${player.Name} trying to load cargo into a ${carrier.Definition.Name} which oes not have a cargo hold"
            )
        }
      case (None, _) | (Some(_), None) =>
        log.warn(
          s"MountVehicleCargoMsg: ${player.Name} lost a vehicle while working with cargo - either $carrier_guid or $cargo_guid"
        )
      case _ => ;
    }
  }

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit = {
    val DismountVehicleCargoMsg(_, cargo_guid, bailed, _, kicked) = pkt
    continent.GUID(cargo_guid) match {
      case Some(cargo: Vehicle) =>
        cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed || kicked)
      case _ => ;
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
  def GetMountableAndSeat(
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
   * If the player is seated in a vehicle, find that vehicle and get the mount index number at which the player is sat.<br>
   * <br>
   * For special purposes involved in zone transfers,
   * where the vehicle may or may not exist in either of the zones (yet),
   * the value of `interstellarFerry` is also polled.
   * Making certain this field is blanked after the transfer is completed is important
   * to avoid inspecting the wrong vehicle and failing simple vehicle checks where this function may be employed.
   * @see `GetMountableAndSeat`
   * @see `interstellarFerry`
   * @return a tuple consisting of a vehicle reference and a mount index
   *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
   *         `(None, None)`, otherwise (even if the vehicle can be determined)
   */
  def GetKnownVehicleAndSeat(): (Option[Vehicle], Option[Int]) =
    GetMountableAndSeat(sessionData.zoning.interstellarFerry, player, continent) match {
      case (Some(v: Vehicle), Some(seat)) => (Some(v), Some(seat))
      case _                              => (None, None)
    }

  /**
   * If the player is seated in a vehicle, find that vehicle and get the mount index number at which the player is sat.
   * @see `GetMountableAndSeat`
   * @return a tuple consisting of a vehicle reference and a mount index
   *         if and only if the vehicle is known to this client and the `WorldSessioNActor`-global `player` occupies it;
   *         `(None, None)`, otherwise (even if the vehicle can be determined)
   */
  def GetVehicleAndSeat(): (Option[Vehicle], Option[Int]) =
    GetMountableAndSeat(None, player, continent) match {
      case (Some(v: Vehicle), Some(seat)) => (Some(v), Some(seat))
      case _                              => (None, None)
    }

  /**
   * Place the current vehicle under the control of the driver's commands,
   * but leave it in a cancellable auto-drive.
   * @param vehicle the vehicle
   */
  def ServerVehicleOverrideStop(vehicle: Vehicle): Unit = {
    val vehicleGuid = vehicle.GUID
    session = session.copy(avatar = avatar.copy(vehicle = Some(vehicleGuid)))
    TotalDriverVehicleControlWithPacket(
      vehicle,
      ServerVehicleOverrideMsg(
        lock_accelerator=false,
        lock_wheel=false,
        reverse=false,
        unk4=true,
        lock_vthrust=0,
        lock_strafe=0,
        movement_speed=vehicle.Definition.AutoPilotSpeed2,
        unk8=None
      )
    )
    sendResponse(PlanetsideAttributeMessage(vehicleGuid, attribute_type=22, attribute_value=0L)) //mount points on
  }

  /**
   * Place the current vehicle under the control of the server's commands.
   * @param vehicle vehicle to be controlled;
   *                the client's player who is receiving this packet should be mounted as its driver, but this is not explicitly tested
   * @param pkt packet to instigate server control
   */
  def ServerVehicleOverrideWithPacket(vehicle: Vehicle, pkt: ServerVehicleOverrideMsg): Unit = {
    serverVehicleControlVelocity = Some(pkt.movement_speed)
    vehicle.DeploymentState = DriveState.AutoPilot
    sendResponse(pkt)
  }

  /**
   * Place the current vehicle under the control of the driver's commands,
   * but leave it in a cancellable auto-drive.
   * Stop all movement entirely.
   * @param vehicle the vehicle
   */
  def ConditionalDriverVehicleControl(vehicle: Vehicle): Unit = {
    if (vehicle.DeploymentState == DriveState.AutoPilot) {
      TotalDriverVehicleControl(vehicle)
    }
  }

  /**
   * Place the current vehicle under the control of the driver's commands,
   * but leave it in a cancellable auto-drive.
   * Stop all movement entirely.
   * @param vehicle the vehicle
   */
  def TotalDriverVehicleControl(vehicle: Vehicle): Unit = {
    TotalDriverVehicleControlWithPacket(
      vehicle,
      ServerVehicleOverrideMsg(
        lock_accelerator=false,
        lock_wheel=false,
        reverse=false,
        unk4=false,
        lock_vthrust=0,
        lock_strafe=0,
        movement_speed=0,
        unk8=None
      )
    )
  }

  /**
   * Place the current vehicle under the control of the driver's commands,
   * but leave it in a cancellable auto-drive.
   * Stop all movement entirely.
   * @param vehicle the vehicle;
   *                the client's player who is receiving this packet should be mounted as its driver, but this is not explicitly tested
   * @param pkt packet to instigate cancellable control
   */
  def TotalDriverVehicleControlWithPacket(vehicle: Vehicle, pkt: ServerVehicleOverrideMsg): Unit = {
    serverVehicleControlVelocity = None
    vehicle.DeploymentState = DriveState.Mobile
    sendResponse(pkt)
  }

  /**
   * Common reporting behavior when a `Deployment` object fails to properly transition between states.
   * @param obj the game object that could not
   * @param state the `DriveState` that could not be promoted
   * @param reason a string explaining why the state can not or will not change
   */
  def CanNotChangeDeployment(
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
