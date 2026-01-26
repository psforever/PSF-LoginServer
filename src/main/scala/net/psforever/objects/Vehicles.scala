// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.transfer.TransferContainer
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.vehicles._
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChatMsg, FrameVehicleStateMessage, GenericObjectActionEnum, HackMessage, HackState, HackState1, HackState7, TriggeredSound, VehicleStateMessage}
import net.psforever.types.{ChatMessageType, DriveState, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

//import scala.concurrent.duration._

object Vehicles {
  private val log = org.log4s.getLogger("Vehicles")

  /**
    * na
    *
    * @param vehicle na
    * @param player  na
    * @return na
    */
  def Own(vehicle: Vehicle, player: Player): Option[Vehicle] = Own(vehicle, Some(player))

  /**
    * na
    * @param vehicle na
    * @param playerOpt na
    * @return na
    */
  def Own(vehicle: Vehicle, playerOpt: Option[Player]): Option[Vehicle] = {
    playerOpt match {
      case Some(tplayer) =>
        tplayer.avatar.vehicle = Some(vehicle.GUID)
        vehicle.AssignOwnership(playerOpt)
        val locked = VehicleLockState.Locked.id
        Array(0, 3).foreach(group => vehicle.PermissionGroup(group, locked))
        Vehicles.ReloadAccessPermissions(vehicle, tplayer.Faction.toString)
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
          vehicle.Zone.id,
          VehicleAction.Ownership(tplayer.GUID, vehicle.GUID)
        )
        Some(vehicle)
      case None =>
        None
    }
  }

  /**
    * Disassociate a vehicle from the player who owns it.
    *
    * @param guid    the unique identifier for that vehicle
    * @param vehicle the vehicle
    * @return the vehicle, if it had a previous owner;
    *         `None`, otherwise
    */
  def Disown(guid: PlanetSideGUID, vehicle: Vehicle): Option[Vehicle] = {
    val zone = vehicle.Zone
    val ownerGuid = vehicle.OwnerGuid
    val ownerName = vehicle.OwnerName
    vehicle.AssignOwnership(None)
    val result = zone
      .GUID(ownerGuid)
      .collect {
        case player: Player => player.avatar
      }
      .orElse {
        zone
          .Players
          .collectFirst {
            case avatar if ownerName.contains(avatar.name) => avatar
          }
      }
      .collect {
        case avatar if avatar.vehicle.contains(guid) =>
          avatar.vehicle = None
          vehicle
      }
    val empire = VehicleLockState.Empire.id
    (0 to 2).foreach(group => vehicle.PermissionGroup(group, empire))
    ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
    zone.VehicleEvents ! VehicleServiceMessage(
      zone.id,
      VehicleAction.LoseOwnership(ownerGuid.getOrElse(Service.defaultPlayerGUID), guid)
    )
    result
  }

  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the specified continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver mount.
    * This is the player side of vehicle ownership removal.
    * @param player the player
    */
  def Disown(player: Player, zone: Zone): Option[Vehicle] = Disown(player, Some(zone))

  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the specified continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver mount.
    * This is the player side of vehicle ownership removal.
    * @param player the player
    */
  def Disown(player: Player, zoneOpt: Option[Zone]): Option[Vehicle] = {
    player.avatar.vehicle match {
      case Some(vehicle_guid) =>
        player.avatar.vehicle = None
        zoneOpt.getOrElse(player.Zone).GUID(vehicle_guid) match {
          case Some(vehicle: Vehicle) =>
            Disown(player, vehicle)
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  /**
   * Disassociate a player from a vehicle that he owns without associating a different player as the owner.
   * Set the vehicle's driver mount permissions and passenger and gunner mount permissions to "allow empire,"
   * then reload them for all clients.
   * This is the vehicle side of vehicle ownership removal.
   * @param player the player
   * @param vehicle the vehicle
   */
  def Disown(player: Player, vehicle: Vehicle): Option[Vehicle] = {
    val pguid = player.GUID
    if (vehicle.OwnerGuid.contains(pguid)) {
      vehicle.AssignOwnership(None)
      //vehicle.Zone.VehicleEvents ! VehicleServiceMessage(player.Name, VehicleAction.Ownership(pguid, PlanetSideGUID(0)))
      //val vguid  = vehicle.GUID
      val empire = VehicleLockState.Empire.id
      (0 to 2).foreach(group => {
        vehicle.PermissionGroup(group, empire)
        /*vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
          s"${vehicle.Faction}",
          VehicleAction.SeatPermissions(pguid, vguid, group, empire)
        )*/
      })
      ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
      Some(vehicle)
    } else {
      None
    }
  }

  /**
    * Iterate over vehicle permissions and turn them into `PlanetsideAttributeMessage` packets.<br>
    * <br>
    * For the purposes of ensuring that other players are always aware of the proper permission state of the trunk and seats,
    * packets are intentionally dispatched to the current client to update the states.
    * Perform this action just after any instance where the client would initially gain awareness of the vehicle.
    * The most important examples include either the player or the vehicle itself spawning in for the first time.
    * @param vehicle the `Vehicle`
    */
  def ReloadAccessPermissions(vehicle: Vehicle, toChannel: String): Unit = {
    val vehicle_guid = vehicle.GUID
    (0 to 3).foreach(group => {
      vehicle.Zone.AvatarEvents ! AvatarServiceMessage(
        toChannel,
        vehicle_guid,
        AvatarAction.PlanetsideAttributeToAll(group + 10, vehicle.PermissionGroup(group).get.id)
      )
    })
  }

  /**
    * A recursive test that explores all the seats of a target vehicle
    * and all the seats of any discovered cargo vehicles
    * and then the same criteria in those cargo vehicles
    * to determine if any of their combined passenger roster remains in a given zone.<br>
    * <br>
    * The original zone is expected to be defined in the internal vehicle gating manifest file
    * and, if this file does not exist, we fail the testing process.
    * The target zone is the one wherever the vehicle currently is located (`vehicle.Zone`).
    * All participant passengers, also defined in the manifest, are expected to be in the target zone at the same time.
    * This test excludes (rejects) same-zone transitions
    * though it would automatically pass the test under those conditions.<br>
    * <br>
    * While it should be possible to recursively explore up a parent-child relationship -
    * testing the ferrying vehicle to which the current tested vehicle is considered a cargo vehicle -
    * the relationship expressed is one of globally unique refertences and not one of object references -
    * that suggested super-ferrying vehicle may not exist in the zone unless special considerations are imposed.
    * For the purpose of these special considerations,
    * implemented by enforcing a strictly downwards order of vehicular zone transportation,
    * where drivers move vehicles and call passengers and immediate cargo vehicle drivers,
    * it becomes unnecessary to test any vehicle that might be ferrying the target vehicle.
    * @see `ZoneAware`
    * @param vehicle the target vehicle being moved around between zones
    * @return `true`, if all passengers of the vehicle, and its cargo vehicles, etc., have reported being in the same zone;
    *        `false`, if no manifest entry exists, or if the vehicle is moving to the same zone
    */
  def AllGatedOccupantsInSameZone(vehicle: Vehicle): Boolean = {
    val vzone = vehicle.Zone
    vehicle.PreviousGatingManifest() match {
      case Some(manifest) if vzone != manifest.origin =>
        val manifestPassengers = manifest.passengers.collect {
          case ManifestPassengerEntry(name, _) => name
        } :+ manifest.driverName
        manifestPassengers.forall { name => vzone.Players.exists(_.name.equals(name)) } &&
        vehicle.CargoHolds.values.forall {
          case hold if hold.isOccupied => AllGatedOccupantsInSameZone(hold.occupant.get)
          case _                       => true
        }
      case _ =>
        false
    }
  }

  /**
    * The orientation of a cargo vehicle as it is being loaded into and contained by a carrier vehicle.
    * The type of carrier is not an important consideration in determining the orientation, oddly enough.
    * @param vehicle the cargo vehicle
    * @return the orientation;
    *         `1` is for unique sideways mounting;
    *         `0` is or straight-on mounting, valid for almost all cases
    */
  def CargoOrientation(vehicle: Vehicle): Int = {
    if (vehicle.Definition == GlobalDefinitions.router || GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition)) {
      1
    } else {
      0
    }
  }

  /**
    * The process of hacking/jacking a vehicle is complete.
    * Change the faction of the vehicle to the hacker's faction and remove all occupants.
    * @param target The `Vehicle` object that has been hacked/jacked
    * @param hacker the one whoi performed the hack and will inherit ownership of the target vehicle
    */
  def FinishHackingVehicle(target: Vehicle, hacker: Player)(): Unit = {
    log.info(s"${hacker.Name} has jacked a ${target.Definition.Name}")
    val tGuid = target.GUID
    val hGuid = hacker.GUID
    val hFaction = hacker.Faction
    val zone = target.Zone
    val zoneid = zone.id
    val vehicleEvents = zone.VehicleEvents
    val localEvents = zone.LocalEvents
    val previousOwnerName = target.OwnerName.getOrElse("")
    vehicleEvents ! VehicleServiceMessage(
      zoneid,
      VehicleAction.SendResponse(
        Service.defaultPlayerGUID,
        HackMessage(HackState1.Unk2, tGuid, hGuid, 100, 0f, HackState.Hacked, HackState7.Unk8)
      )
    )
    target.Actor ! CommonMessages.Hack(hacker, target)
    // Forcefully dismount any cargo
    target.CargoHolds.foreach { case (_, cargoHold) =>
      cargoHold.occupant.collect {
        cargo: Vehicle => cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed = false)
      }
    }
    // Forcefully dismount all seated occupants from the vehicle
    target.Seats.values.foreach(seat => {
      seat.occupant.collect {
        player: Player =>
          seat.unmount(player)
          player.VehicleSeated = None
          vehicleEvents ! VehicleServiceMessage(
            zoneid,
            VehicleAction.KickPassenger(player.GUID, 4, unk2 = false, tGuid)
          )
      }
      // In case BFR is occupied and may or may not be crouched
      if (GlobalDefinitions.isBattleFrameVehicle(target.Definition) && target.Seat(0).isDefined) {
        zone.LocalEvents ! LocalServiceMessage(
          zoneid,
          PlanetSideGUID(-1),
          LocalAction.GenericObjectAction(target.GUID, GenericObjectActionEnum.BFRShieldsDown)
        )
        zone.LocalEvents ! LocalServiceMessage(
          zoneid,
          LocalAction.SendResponse(
            FrameVehicleStateMessage(target.GUID, 0, target.Position, target.Orientation, Some(Vector3(0f, 0f, 0f)), unk2=false, 0, 0, is_crouched=true, is_airborne=false, ascending_flight=false, 10, 0, 0)))
        zone.LocalEvents ! LocalServiceMessage(
          zoneid,
          LocalAction.SendResponse(
            VehicleStateMessage(target.GUID, 0, target.Position, target.Orientation, Some(Vector3(0f, 0f, 0f)), None, 0, 0, 15, is_decelerating=false, is_cloaked=false)))
      }
    })
    // If the vehicle can fly and is flying: deconstruct it; and well played to whomever managed to hack a plane in mid air
    if (target.Definition.CanFly && target.isFlying) {
      // todo: Should this force the vehicle to land in the same way as when a pilot bails with passengers on board?
      target.Actor ! Vehicle.Deconstruct()
    } else { // Otherwise handle ownership transfer as normal
      // Remove ownership of our current vehicle, if we have one
      hacker.avatar.vehicle
        .flatMap { guid => zone.GUID(guid) }
        .collect { case vehicle: Vehicle =>
          Vehicles.Disown(hacker, vehicle)
        }
      // Remove ownership of the vehicle from the previous player
      target.OwnerGuid
        .flatMap { guid => zone.GUID(guid) }
        .collect { case tplayer: Player =>
          Vehicles.Disown(tplayer, target)
        }
      // Now take ownership of the jacked vehicle
      target.Faction = hFaction
      Vehicles.Own(target, hacker)
      //todo: Send HackMessage -> HackCleared to vehicle? can be found in packet captures. Not sure if necessary.
      // And broadcast the faction change to other clients
      zone.AvatarEvents ! AvatarServiceMessage(
        zoneid,
        AvatarAction.SetEmpire(tGuid, hFaction)
      )
    }
    localEvents ! LocalServiceMessage(
      zoneid,
      hGuid,
      LocalAction.TriggerSound(TriggeredSound.HackVehicle, target.Position, 30, 0.49803925f)
    )
    if (zone.Players.exists(_.name.equals(previousOwnerName))) {
      localEvents ! LocalServiceMessage(
        previousOwnerName,
        LocalAction.SendResponse(ChatMsg(ChatMessageType.UNK_226, "@JackStolen"))
      )
    }
    localEvents ! LocalServiceMessage(
      hacker.Name,
      LocalAction.SendResponse(ChatMsg(ChatMessageType.UNK_226, "@JackVehicleOwned"))
    )
    // Clean up after specific vehicles, e.g. remove router telepads
    // If AMS is deployed, swap it to the new faction
    target.Definition match {
      case GlobalDefinitions.router =>
        target.Utility(UtilityType.internal_router_telepad_deployable).collect {
          case util: Utility.InternalTelepad =>
            //"power cycle"
            util.Actor ! TelepadLike.Deactivate(util)
            util.Actor ! TelepadLike.Activate(util)
        }
      case GlobalDefinitions.ams if target.DeploymentState == DriveState.Deployed =>
        vehicleEvents ! VehicleServiceMessage.AMSDeploymentChange(zone)
      case _ => ()
    }
    vehicleEvents ! VehicleServiceMessage(
      zoneid,
      VehicleAction.SendResponse(
        Service.defaultPlayerGUID,
        HackMessage(HackState1.Unk2, tGuid, tGuid, 0, 1L, HackState.HackCleared, HackState7.Unk8)
      )
    )
    target.Actor ! CommonMessages.ClearHack()
  }

  def FindANTChargingSource(
      obj: TransferContainer,
      ntuChargingTarget: Option[TransferContainer]
  ): Option[TransferContainer] = {
    //determine if we are close enough to charge from something
    (ntuChargingTarget match {
      case Some(target: WarpGate) if {
            val soiRadius = target.Definition.SOIRadius
            Vector3.DistanceSquared(obj.Position.xy, target.Position.xy) < soiRadius * soiRadius
          } =>
        Some(target.asInstanceOf[NtuContainer])
      case _ =>
        None
    }).orElse {
      val position = obj.Position.xy
      obj.Zone.Buildings.values
        .collectFirst {
          case gate: WarpGate if {
                val soiRadius = gate.Definition.SOIRadius
                Vector3.DistanceSquared(position, gate.Position.xy) < soiRadius * soiRadius
              } =>
            gate
        }
        .asInstanceOf[Option[NtuContainer]]
    }
  }

  def FindANTDischargingTarget(
                                obj: TransferContainer,
                                ntuChargingTarget: Option[TransferContainer]
                              ): Option[TransferContainer] = {
    FindResourceSiloToDischargeInto(obj, ntuChargingTarget, radius = 20)
  }

  def FindBfrChargingSource(
                             obj: TransferContainer,
                             ntuChargingTarget: Option[TransferContainer]
                           ): Option[TransferContainer] = {
    //determine if we are close enough to charge from something
    val position = obj.Position.xy
    ntuChargingTarget.orElse(
      obj.Zone
        .blockMap
        .sector(position, range = 20f).buildingList
        .sortBy { b => Vector3.DistanceSquared(position, b.Position.xy) }
        .flatMap { _.NtuSource }
        .headOption
    ) match {
      case out @ Some(_: WarpGate) =>
        out
      case Some(silo: ResourceSilo) if {
        val radius = 20f//3.6135f
        Vector3.DistanceSquared(position, silo.Position.xy) < radius * radius && obj.Faction != silo.Faction
      } =>
        Some(silo)
      case _ =>
        None
    }
  }

  def FindBfrDischargingTarget(
                                obj: TransferContainer,
                                ntuChargingTarget: Option[TransferContainer]
                              ): Option[TransferContainer] = {
    FindResourceSiloToDischargeInto(obj, ntuChargingTarget, radius = 20) //3.6135f?
  }

  def FindResourceSiloToDischargeInto(
                                obj: TransferContainer,
                                ntuChargingTarget: Option[TransferContainer],
                                radius: Float
                              ): Option[TransferContainer] = {
    //determine if we are close enough to charge from something
    val position = obj.Position.xy
    ntuChargingTarget.orElse(
      obj.Zone
        .blockMap
        .sector(position, range = 20f)
        .buildingList
        .sortBy { b => Vector3.DistanceSquared(position, b.Position.xy) }
        .flatMap { _.NtuSource }
        .headOption
    ) match {
      case out @ Some(silo: ResourceSilo)
        if Vector3.DistanceSquared(position, silo.Position.xy) < radius * radius &&
           silo.Faction == PlanetSideEmpire.NEUTRAL || silo.Faction == obj.Faction =>
        out
      case _ =>
        None
    }
  }

  /**
    * Before a vehicle is removed from the game world, the following actions must be performed.
    *
    * @param vehicle the vehicle
    */
  //noinspection ScalaUnusedSymbol
  def BeforeUnloadVehicle(vehicle: Vehicle, zone: Zone): Unit = {
    vehicle.Definition match {
      case GlobalDefinitions.ams =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case GlobalDefinitions.ant =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case _ => ()
    }
  }

  /**
    * Find the position and angle at which an ejected player will be placed once outside of the shuttle.
    * Mainly for use with the proper high altitude rapid transport (HART) shuttle and it's corresponding HART building.
    * @param obj the (shuttle) vehicle
    * @param mountPoint the mount point that indicates a seat
    * @return the position and angle
    */
  def dismountShuttle(obj: Vehicle, mountPoint: Int): (Vector3, Float) = {
    val shuttleAngle = obj.Orientation.z
    val offset = {
      val baseOffset = obj.MountPoints(mountPoint).positionOffset
      Vector3.Rz(baseOffset.xy, shuttleAngle) + Vector3.z(baseOffset.z)
    }
    val turnAway = if (offset.x >= 0) -90f else 90f
    (obj.Position + offset, (shuttleAngle + turnAway) % 360f)
  }

  /**
   * Based on a mounting index, for a certain mount, to what mounting group does this seat belong?
   * @param vehicle the vehicle
   * @param seatNumber specific seat index
   * @return the seat group designation
   */
  def SeatPermissionGroup(vehicle: Vehicle, seatNumber: Int): Option[AccessPermissionGroup.Value] = {
    SeatPermissionGroup(vehicle.Definition, seatNumber)
  }

  /**
   * Based on a mounting index, for a certain mount, to what mounting group does this seat belong?
   * @param definition global vehicle specification
   * @param seatNumber specific seat index
   * @return the seat group designation
   */
  def SeatPermissionGroup(definition: VehicleDefinition, seatNumber: Int): Option[AccessPermissionGroup.Value] = {
    if (seatNumber == 0) { //valid in almost all cases
      Some(AccessPermissionGroup.Driver)
    } else {
      definition.Seats
        .get(seatNumber)
        .map { _ =>
          definition.controlledWeapons()
            .get(seatNumber)
            .map { _ => AccessPermissionGroup.Gunner }
            .getOrElse { AccessPermissionGroup.Passenger }
        }
        .orElse {
          definition.Cargo
            .get(seatNumber)
            .map { _ => AccessPermissionGroup.Passenger }
            .orElse {
              val offset = definition.TrunkOffset
              val size = definition.TrunkSize
              if (seatNumber >= offset && seatNumber < offset + size.Width * size.Height) {
                Some(AccessPermissionGroup.Trunk)
              } else {
                None
              }
            }
        }
    }
  }
}
