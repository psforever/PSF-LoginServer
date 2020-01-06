// Copyright (c) 2019 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataWithPlacement, FlightPhysics, PlacementData, RemoteProjectileData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class ProjectileConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj : Projectile) : Try[RemoteProjectileData] = {
    Success(
      RemoteProjectileData(
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
        obj.profile.RemoteClientData._1,
        obj.profile.RemoteClientData._2,
        FlightPhysics.State4,
        0,
        0
      )
    )
  }

  override def DetailedConstructorData(obj : Projectile) : Try[RemoteProjectileData] =
    Failure(new Exception("ProjectileConverter should not be used to generate detailed projectile data (nothing should)"))
}
