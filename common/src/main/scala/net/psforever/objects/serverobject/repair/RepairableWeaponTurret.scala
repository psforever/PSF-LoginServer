//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.Tool
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.types.PlanetSideGUID
import services.vehicle.{VehicleAction, VehicleServiceMessage}

trait RepairableWeaponTurret extends RepairableAmenity {
  override def RepairValue(item : Tool) : Int = item.FireMode.Modifiers.Damage1

  override def Restoration(obj : Repairable.Target) : Unit = {
    obj match {
      case turret : Repairable.Target with WeaponTurret =>
        super.Restoration(obj)
        val zone = obj.Zone
        val tguid = obj.GUID
        turret.Weapons
          .map({ case (index, slot) => (index, slot.Equipment) })
          .collect { case (index, Some(tool : Tool)) =>
            zone.VehicleEvents ! VehicleServiceMessage(
              zone.Id,
              VehicleAction.EquipmentInSlot(PlanetSideGUID(0), tguid, index, tool)
            )
          }
      case _ => ;
    }
  }
}
