// Copyright (c) 2025 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.Vehicle
import net.psforever.objects.avatar.interaction.{ForceZoneProtection, InteractWithForceDomeProtection}
import net.psforever.objects.serverobject.dome.ForceDomePhysics
import net.psforever.objects.serverobject.mount.interaction.InteractWithForceDomeProtectionSeatedInEntity
import net.psforever.objects.zones.interaction.InteractsWithZone

class InteractWithForceDomeProtectionSeatedInVehicle
  extends InteractWithForceDomeProtectionSeatedInEntity {
  override def applyProtection(target: InteractsWithZone, dome: ForceDomePhysics): Unit = {
    super.applyProtection(target, dome)
    target
      .asInstanceOf[Vehicle]
      .CargoHolds
      .values
      .flatMap(_.occupants)
      .foreach { vehicle =>
        vehicle
          .interaction()
          .find(_.Type == ForceZoneProtection)
          .foreach {
            case interaction: InteractWithForceDomeProtection =>
              interaction.applyProtection(vehicle, dome)
            case _ => ()
          }
      }
  }

  override def resetInteraction(target: InteractsWithZone): Unit = {
    super.resetInteraction(target)
    target
      .asInstanceOf[Vehicle]
      .CargoHolds
      .values
      .flatMap(_.occupants)
      .foreach { vehicle =>
        vehicle
          .interaction()
          .find(_.Type == ForceZoneProtection)
          .foreach {
            case interaction: InteractWithForceDomeProtection =>
              interaction.resetInteraction(vehicle)
            case _ => ()
          }
      }
  }
}
