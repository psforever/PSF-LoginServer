// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.TurretDeployable
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

object SmallTurretConverter extends ObjectCreateConverter[TurretDeployable] {
  override def ConstructorData(obj: TurretDeployable): Try[SmallTurretData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 0) {
      Success(
        SmallTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, None, obj.Jammed, None, obj.OwnerGuid match {
                case Some(owner) => owner
                case None        => PlanetSideGUID(0)
              })
          ),
          health,
          Some(InventoryData(TurretConverter.MakeMountings(obj)))
        )
      )
    } else {
      Success(
        SmallTurretData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(obj.Faction, bops = false, alternate = true, v1 = false, None, jammered = false, None, PlanetSideGUID(0))
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj: TurretDeployable): Try[SmallTurretData] =
    Failure(new Exception("converter should not be used to generate detailed SmallTurretData"))
}
