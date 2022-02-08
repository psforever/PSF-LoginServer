// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.transfer.TransferContainer
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.vehicles._
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.TriggeredSound
import net.psforever.types.{DriveState, PlanetSideEmpire, PlanetSideGUID, Vector3}
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
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
          vehicle.Zone.id,
          VehicleAction.Ownership(tplayer.GUID, vehicle.GUID)
        )
        Vehicles.ReloadAccessPermissions(vehicle, tplayer.Name)
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
  def Disown(guid: PlanetSideGUID, vehicle: Vehicle): Option[Vehicle] =
    vehicle.Zone.GUID(vehicle.Owner) match {
      case Some(player: Player) =>
        if (player.avatar.vehicle.contains(guid)) {
          player.avatar.vehicle = None
          vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
            player.Name,
            VehicleAction.Ownership(player.GUID, PlanetSideGUID(0))
          )
        }
        vehicle.AssignOwnership(None)
        val empire         = VehicleLockState.Empire.id
        val factionChannel = s"${vehicle.Faction}"
        (0 to 2).foreach(group => {
          vehicle.PermissionGroup(group, empire)
          vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
            factionChannel,
            VehicleAction.SeatPermissions(Service.defaultPlayerGUID, guid, group, empire)
          )
        })
        ReloadAccessPermissions(vehicle, player.Name)
        Some(vehicle)
      case _ =>
        None
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
    */
  def Disown(player: Player, vehicle: Vehicle): Option[Vehicle] = {
    val pguid = player.GUID
    if (vehicle.Owner.contains(pguid)) {
      vehicle.AssignOwnership(None)
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(player.Name, VehicleAction.Ownership(pguid, PlanetSideGUID(0)))
      val vguid  = vehicle.GUID
      val empire = VehicleLockState.Empire.id
      (0 to 2).foreach(group => {
        vehicle.PermissionGroup(group, empire)
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
          s"${vehicle.Faction}",
          VehicleAction.SeatPermissions(pguid, vguid, group, empire)
        )
      })
      ReloadAccessPermissions(vehicle, player.Name)
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
        AvatarAction.PlanetsideAttributeToAll(vehicle_guid, group + 10, vehicle.PermissionGroup(group).get.id)
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
    * @param unk na; used by `HackMessage` as `unk5`
    */
  def FinishHackingVehicle(target: Vehicle, hacker: Player, unk: Long)(): Unit = {
    log.info(s"${hacker.Name} has jacked a ${target.Definition.Name}")
    val zone = target.Zone
    // Forcefully dismount any cargo
    target.CargoHolds.foreach { case (_, cargoHold) =>
      cargoHold.occupant match {
        case Some(cargo: Vehicle) =>
          cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed = false)
        case None => ;
      }
    }
    // Forcefully dismount all seated occupants from the vehicle
    target.Seats.values.foreach(seat => {
      seat.occupant match {
        case Some(tplayer: Player) =>
          seat.unmount(tplayer)
          tplayer.VehicleSeated = None
          if (tplayer.HasGUID) {
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.id,
              VehicleAction.KickPassenger(tplayer.GUID, 4, unk2 = false, target.GUID)
            )
          }
        case _ => ;
      }
    })
    // If the vehicle can fly and is flying deconstruct it, and well played to whomever managed to hack a plane in mid air. I'm impressed.
    if (target.Definition.CanFly && target.isFlying) {
      // todo: Should this force the vehicle to land in the same way as when a pilot bails with passengers on board?
      target.Actor ! Vehicle.Deconstruct()
    } else { // Otherwise handle ownership transfer as normal
      // Remove ownership of our current vehicle, if we have one
      hacker.avatar.vehicle match {
        case Some(guid: PlanetSideGUID) =>
          zone.GUID(guid) match {
            case Some(vehicle: Vehicle) =>
              Vehicles.Disown(hacker, vehicle)
            case _ => ;
          }
        case _ => ;
      }
      target.Owner match {
        case Some(previousOwnerGuid: PlanetSideGUID) =>
          // Remove ownership of the vehicle from the previous player
          zone.GUID(previousOwnerGuid) match {
            case Some(tplayer: Player) =>
              Vehicles.Disown(tplayer, target)
            case _ => ; // Vehicle already has no owner
          }
        case _ => ;
      }
      // Now take ownership of the jacked vehicle
      target.Actor ! CommonMessages.Hack(hacker, target)
      target.Faction = hacker.Faction
      Vehicles.Own(target, hacker)
      //todo: Send HackMessage -> HackCleared to vehicle? can be found in packet captures. Not sure if necessary.
      // And broadcast the faction change to other clients
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.SetEmpire(Service.defaultPlayerGUID, target.GUID, hacker.Faction)
      )
    }
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.TriggerSound(hacker.GUID, TriggeredSound.HackVehicle, target.Position, 30, 0.49803925f)
    )
    // Clean up after specific vehicles, e.g. remove router telepads
    // If AMS is deployed, swap it to the new faction
    target.Definition match {
      case GlobalDefinitions.router =>
        target.Utility(UtilityType.internal_router_telepad_deployable) match {
          case Some(util: Utility.InternalTelepad) =>
            //"power cycle"
            util.Actor ! TelepadLike.Deactivate(util)
            util.Actor ! TelepadLike.Activate(util)
          case _ => ;
        }
      case GlobalDefinitions.ams if target.DeploymentState == DriveState.Deployed =>
        zone.VehicleEvents ! VehicleServiceMessage.AMSDeploymentChange(zone)
      case _ => ;
    }
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
  def BeforeUnloadVehicle(vehicle: Vehicle, zone: Zone): Unit = {
    vehicle.Definition match {
      case GlobalDefinitions.ams =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case GlobalDefinitions.ant =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        vehicle.Actor ! Deployment.TryUndeploy(DriveState.Undeploying)
      case _ => ;
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
}
