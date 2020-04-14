// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.{Player, Tool}
import net.psforever.packet.game.InventoryStateMessage
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.VehicleServiceMessage
import services.vehicle.support.TurretUpgrader

object WeaponTurrets {
  private val log = org.log4s.getLogger("WeaponTurrets")

  /**
    * The process of upgrading a turret's weapon(s) is completed.
    * Pass the message onto the turret and onto the vehicle events system.
    * Additionally, force-deplete the ammunition count of the nano-dispenser used to perform the upgrade.
    * @param target the turret
    * @param tool the nano-dispenser that was used to perform this upgrade
    * @param upgrade the new upgrade state
    */
  def FinishUpgradingMannedTurret(target : FacilityTurret, user : Player, tool : Tool, upgrade : TurretUpgrade.Value)() : Unit = {
    tool.Magazine = 0
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      user.Name,
      AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(tool.AmmoSlot.Box.GUID, tool.GUID, 0))
    )
    FinishUpgradingMannedTurret(target, upgrade)
  }

  /**
    * The process of upgrading a turret's weapon(s) is completed.
    * * Pass the message onto the turret and onto the vehicle events system.
    * @see `FacilityTurret`
    * @see `TurretUpgrade`
    * @see `TurretUpgrader.AddTask`
    * @see `TurretUpgrader.ClearSpecific`
    * @see `VehicleServiceMessage.TurretUpgrade`
    * @param target the facility turret being upgraded
    * @param upgrade the upgrade being applied to the turret (usually, it's weapon system)
    */
  def FinishUpgradingMannedTurret(target : FacilityTurret, upgrade : TurretUpgrade.Value) : Unit = {
    log.info(s"Converting manned wall turret weapon to $upgrade")
    val zone = target.Zone
    val events = zone.VehicleEvents
    events ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.ClearSpecific(List(target), zone))
    events ! VehicleServiceMessage.TurretUpgrade(TurretUpgrader.AddTask(target, zone, upgrade))
  }
}

