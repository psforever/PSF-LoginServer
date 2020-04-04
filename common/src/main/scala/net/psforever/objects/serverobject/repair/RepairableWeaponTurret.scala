//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.Tool
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.objects.vehicles.MountedWeapons
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

/**
  * The "control" `Actor` mixin for repair-handling code for `WeaponTurret` objects.
  */
trait RepairableWeaponTurret extends RepairableEntity {
  def RepairableObject : Repairable.Target with WeaponTurret

  override def Restoration(target : Repairable.Target) : Unit = {
    super.Restoration(target)
    RepairableWeaponTurret.Restoration(RepairableObject)
  }
}

object RepairableWeaponTurret {
  /**
    * A restored target dispatches messages to reconstruct the weapons that were previously mounted to the turret
    * and may have been concealed/deleted when the target was destroyed.
    * @see `MountedWeapons`
    * @see `MountedWeapons.Weapons`
    * @see `Service.defaultPlayerGUID`
    * @see `WeaponTurret`
    * @see `VehicleAction.EquipmentInSlot`
    * @see `VehicleServiceMessage`
    * @see `Zone.VehicleEvents`
    * @param target the entity being destroyed;
    *               note: `MountedWeapons` is a parent of `WeaponTurret`
    *               but the handling code closely associates with the former
    */
  def Restoration(target : Repairable.Target with MountedWeapons) : Unit = {
    val zone = target.Zone
    val zoneId = zone.Id
    val tguid = target.GUID
    val events = zone.VehicleEvents
    target.Weapons
      .map({ case (index, slot) => (index, slot.Equipment) })
      .collect { case (index, Some(tool : Tool)) =>
        events ! VehicleServiceMessage(
          zoneId,
          VehicleAction.EquipmentInSlot(Service.defaultPlayerGUID, tguid, index, tool)
        )
      }
  }
}
