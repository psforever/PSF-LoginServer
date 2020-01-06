// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.TrapDeployable
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class TRAPConverter extends ObjectCreateConverter[TrapDeployable]() {
  override def ConstructorData(obj : TrapDeployable) : Try[TRAPData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if(health > 0) {
      Success(
        TRAPData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = false,
              true,
              None,
              false,
              Some(true),
              None,
              obj.Owner match {
                case Some(owner) => owner
                case None => PlanetSideGUID(0)
              }
            )
          ),
          health
        )
      )
    }
    else {
      Success(
        TRAPData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = true,
              true,
              None,
              false,
              Some(true),
              None,
              PlanetSideGUID(0)
            )
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj : TrapDeployable) : Try[TRAPData] =
    Failure(new Exception("converter should not be used to generate detailed TRAPData"))
}
