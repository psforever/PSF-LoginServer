// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.packet.game.{ChildObjectStateMessage, DeployRequestMessage, VehicleSubStateMessage, _}
import net.psforever.types.DriveState

trait VehicleFunctions extends CommonSessionInterfacingFunctionality {
  def ops: VehicleOperations

  def handleVehicleState(pkt: VehicleStateMessage): Unit

  def handleFrameVehicleState(pkt: FrameVehicleStateMessage): Unit

  def handleChildObjectState(pkt: ChildObjectStateMessage): Unit

  def handleVehicleSubState(pkt: VehicleSubStateMessage): Unit

  def handleDeployRequest(pkt: DeployRequestMessage): Unit

  /* messages */

  def handleCanDeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit

  def handleCanUndeploy(obj: Deployment.DeploymentObject, state: DriveState.Value): Unit

  def handleCanNotChangeDeployment(obj: Deployment.DeploymentObject, state: DriveState.Value, reason: String): Unit
}

class VehicleOperations(
                         val sessionLogic: SessionData,
                         val avatarActor: typed.ActorRef[AvatarActor.Command],
                         implicit val context: ActorContext
                       ) extends CommonSessionInterfacingFunctionality {
  private[session] var serverVehicleControlVelocity: Option[Int] = None

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
    GetMountableAndSeat(sessionLogic.zoning.interstellarFerry, player, continent) match {
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
  private def TotalDriverVehicleControlWithPacket(vehicle: Vehicle, pkt: ServerVehicleOverrideMsg): Unit = {
    serverVehicleControlVelocity = None
    vehicle.DeploymentState = DriveState.Mobile
    sendResponse(pkt)
  }
}
