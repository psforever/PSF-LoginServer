// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.Player
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}

trait MountableTurretControl
  extends TurretControl
    with MountableBehavior {
  override def TurretObject: PlanetSideServerObject with WeaponTurret with Mountable

  /** commonBehavior does not implement mountingBehavior; please do so when implementing */
  override def commonBehavior: Receive =
    super.commonBehavior
      .orElse(dismountBehavior)

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    player: Player
                                  ): Boolean = MountableTurret.MountTest(TurretObject, player)
}

object MountableTurret {
  def MountTest(obj: PlanetSideServerObject with WeaponTurret with Mountable, player: Player): Boolean = {
    (!obj.Definition.FactionLocked || player.Faction == obj.Faction) && !obj.Destroyed
  }
}
