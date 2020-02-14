// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.vehicles.VehicleLockState
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

object Vehicles {
  /**
    * na
    * @param vehicle na
    * @param tplayer na
    * @return na
    */
  def Own(vehicle : Vehicle, tplayer : Player) : Option[Vehicle] = Own(vehicle, Some(tplayer))

  /**
    * na
    * @param vehicle na
    * @param playerOpt na
    * @return na
    */
  def Own(vehicle : Vehicle, playerOpt : Option[Player]) : Option[Vehicle] = {
    playerOpt match {
      case Some(tplayer) =>
        tplayer.VehicleOwned = vehicle.GUID
        vehicle.AssignOwnership(playerOpt)
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(vehicle.Zone.Id, VehicleAction.Ownership(tplayer.GUID, vehicle.GUID))
        Vehicles.ReloadAccessPermissions(vehicle, tplayer.Name)
        Some(vehicle)
      case None =>
        None
    }
  }

  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the specified continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver seat.
    * This is the player side of vehicle ownership removal.
    * @param player the player
    */
  def Disown(player : Player, zone : Zone) : Option[Vehicle] = Disown(player, Some(zone))
  /**
    * Disassociate a player from a vehicle that he owns.
    * The vehicle must exist in the game world on the specified continent.
    * This is similar but unrelated to the natural exchange of ownership when someone else sits in the vehicle's driver seat.
    * This is the player side of vehicle ownership removal.
    * @param player the player
    */
  def Disown(player : Player, zoneOpt : Option[Zone]) : Option[Vehicle] = {
    player.VehicleOwned match {
      case Some(vehicle_guid) =>
        player.VehicleOwned = None
        zoneOpt.getOrElse(player.Zone).GUID(vehicle_guid) match {
          case Some(vehicle : Vehicle) =>
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
    * Set the vehicle's driver seat permissions and passenger and gunner seat permissions to "allow empire,"
    * then reload them for all clients.
    * This is the vehicle side of vehicle ownership removal.
    * @param player the player
    */
  def Disown(player : Player, vehicle : Vehicle) : Option[Vehicle] = {
    val pguid = player.GUID
    if(vehicle.Owner.contains(pguid)) {
      vehicle.AssignOwnership(None)
      val factionChannel = s"${vehicle.Faction}"
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(factionChannel, VehicleAction.Ownership(pguid, PlanetSideGUID(0)))
      val vguid = vehicle.GUID
      val empire = VehicleLockState.Empire.id
      (0 to 2).foreach(group => {
        vehicle.PermissionGroup(group, empire)
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(factionChannel, VehicleAction.SeatPermissions(pguid, vguid, group, empire))
      })
      ReloadAccessPermissions(vehicle, player.Name)
      Some(vehicle)
    }
    else {
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
  def ReloadAccessPermissions(vehicle : Vehicle, toChannel : String) : Unit = {
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
  def AllGatedOccupantsInSameZone(vehicle : Vehicle) : Boolean = {
    val vzone = vehicle.Zone
    vehicle.PreviousGatingManifest() match {
      case Some(manifest) if vzone != manifest.origin =>
        val manifestPassengers = manifest.passengers.collect { case (name, _) => name } :+ manifest.driverName
        val manifestPassengerResults = manifestPassengers.map { name => vzone.Players.exists(_.name.equals(name)) }
        manifestPassengerResults.forall(_ == true) &&
          vehicle.CargoHolds.values
            .collect { case hold if hold.isOccupied => AllGatedOccupantsInSameZone(hold.Occupant.get) }
            .forall(_ == true)
      case _ =>
        false
    }
  }
}
