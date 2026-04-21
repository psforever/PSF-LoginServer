// Copyright (c) 2025 PSForever
package net.psforever.objects.serverobject.mount.interaction

import net.psforever.objects.avatar.interaction.InteractWithForceDomeProtection
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.dome.ForceDomePhysics
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.zones.interaction.InteractsWithZone

class InteractWithForceDomeProtectionSeatedInEntity
extends InteractWithForceDomeProtection {
  override def range: Float = 30f

  override def applyProtection(target: InteractsWithZone, dome: ForceDomePhysics): Unit = {
    super.applyProtection(target, dome)
    target
      .asInstanceOf[Mountable]
      .Seats
      .values
      .flatMap(_.occupants)
      .foreach { occupant =>
        occupant.Actor ! Damageable.MakeInvulnerable
      }
  }

  override def resetInteraction(target: InteractsWithZone): Unit = {
    super.resetInteraction(target)
    target
      .asInstanceOf[Mountable]
      .Seats
      .values
      .flatMap(_.occupants)
      .foreach { occupant =>
        occupant.Actor ! Damageable.MakeVulnerable
      }
  }
}
