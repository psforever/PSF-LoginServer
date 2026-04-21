// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.Player
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.sourcing.{PlayerSource, TurretSource}
import net.psforever.objects.vital.{DismountingActivity, MountingActivity}

import scala.annotation.unused

trait MountableTurretControl
  extends TurretControl
    with MountableBehavior {
  override def TurretObject: PlanetSideServerObject with WeaponTurret with Mountable

  /** commonBehavior does not implement mountingBehavior; please do so when implementing */
  override def commonBehavior: Receive = super.commonBehavior.orElse(dismountBehavior)

  override def mountActionResponse(user: Player, @unused mountPoint: Int, seatNumber: Int): Unit = {
    super.mountActionResponse(user, mountPoint, seatNumber)
    if (TurretObject.PassengerInSeat(user).contains(0)) {
      val vsrc = TurretSource(TurretObject)
      user.LogActivity(MountingActivity(vsrc, PlayerSource.inSeat(user, vsrc, seatNumber = 0), TurretObject.Zone.Number))
    }
  }

  override def dismountActionResponse(user: Player, seatBeingDismounted: Int): Unit = {
    super.dismountActionResponse(user, seatBeingDismounted)
    if (!TurretObject.Seats(seatBeingDismounted).isOccupied) { //this seat really was vacated
      user.LogActivity(DismountingActivity(TurretSource(TurretObject), PlayerSource(user), TurretObject.Zone.Number))
    }
  }

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
