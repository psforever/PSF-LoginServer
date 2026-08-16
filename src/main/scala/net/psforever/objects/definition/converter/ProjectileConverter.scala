// Copyright (c) 2019 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ballistics.Projectile
import net.psforever.packet.objectcreate.{CommonFieldData, CommonFieldDataWithPlacement, FlightPhysics, PlacementData, RemoteProjectileData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class ProjectileConverter extends ObjectCreateConverter[Projectile]() {
  override def ConstructorData(obj: Projectile): Try[RemoteProjectileData] = {
    Success(
      RemoteProjectileData(
        CommonFieldDataWithPlacement(
          PlacementData(
            obj.Position,
            obj.Orientation,
            obj.Velocity
          ),
          CommonFieldData(obj.owner.Faction, bops = false, alternate = false, v1 = true, v2= None, jammered = false, v5 = None, guid = PlanetSideGUID(0))
        ),
        u1 = obj.profile.RemoteClientData._1,
        u2 = obj.profile.RemoteClientData._2,
        FlightPhysics.State4,
        unk4 = 0,
        unk5 = 0
      )
    )
  }

  override def DetailedConstructorData(obj: Projectile): Try[RemoteProjectileData] =
    Failure(
      new Exception("ProjectileConverter should not be used to generate detailed projectile data (nothing should)")
    )
}
