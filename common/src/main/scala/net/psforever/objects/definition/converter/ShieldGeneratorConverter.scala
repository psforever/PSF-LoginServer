// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ShieldGeneratorDeployable
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class ShieldGeneratorConverter extends ObjectCreateConverter[ShieldGeneratorDeployable]() {
  override def ConstructorData(obj : ShieldGeneratorDeployable) : Try[AegisShieldGeneratorData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if(health > 0) {
      Success(
        AegisShieldGeneratorData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = false,
              v1 = false,
              v2 = None,
              jammered = obj.Jammed,
              None,
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
        AegisShieldGeneratorData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = true,
              v1 = false,
              v2 = None,
              jammered = obj.Jammed,
              None,
              None,
              PlanetSideGUID(0)
            )
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj : ShieldGeneratorDeployable) : Try[AegisShieldGeneratorData] =
    Failure(new Exception("converter should not be used to generate detailed ShieldGeneratorDdata"))
}
