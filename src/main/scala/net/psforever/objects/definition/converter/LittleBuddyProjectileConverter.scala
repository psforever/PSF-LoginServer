// Copyright (c) 2022 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataWithPlacement, FlightPhysics, PlacementData, RemoteProjectileData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Success, Try}

class LittleBuddyProjectileConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj: Projectile): Try[RemoteProjectileData] = lilBudData(obj)

  override def DetailedConstructorData(obj: Projectile): Try[RemoteProjectileData] = lilBudData(obj)

  private def lilBudData(obj: Projectile): Try[RemoteProjectileData] = {
    Success(
      RemoteProjectileData(
        CommonFieldDataWithPlacement(
          PlacementData(
            obj.Position,
            obj.Orientation,
            obj.Velocity
          ),
          CommonFieldData(obj.owner.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = None, guid = PlanetSideGUID(0))
        ),
        0,
        0,
        FlightPhysics.State3,
        7,
        2
      )
    )
  }
}
