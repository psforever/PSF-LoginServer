// Copyright (c) 2019 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataWithPlacement, FlightPhysics, PlacementData, TrackedProjectileData}

import scala.util.{Failure, Success, Try}

class ProjectileConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj : Projectile) : Try[TrackedProjectileData] = {
    Success(
      TrackedProjectileData(
        CommonFieldDataWithPlacement(
          PlacementData(
            obj.Position,
            obj.Orientation,
            obj.Velocity
          ),
          CommonFieldData(
            obj.owner.Faction,
            false,
            false,
            true,
            None,
            false,
            None,
            None,
            PlanetSideGUID(0)
          )
        ),
        0,
        0,
        FlightPhysics.State3,
        7,
        2
      )
    )
  }

  override def DetailedConstructorData(obj : Projectile) : Try[TrackedProjectileData] =
    Failure(new Exception("ProjectileConverter should not be used to generate detailed projectile data (nothing should)"))
}
