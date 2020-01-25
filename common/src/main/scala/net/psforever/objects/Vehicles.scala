// Copyright (c) 2020 PSForever
package net.psforever.objects

import net.psforever.objects.vehicles.VehicleLockState
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

object Vehicles {
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
    * Uterate over vehicle permissions and turn them into `PlanetsideAttributeMessage` packets.<br>
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
}
