// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ShieldGeneratorDeployable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class ShieldGeneratorConverter extends ObjectCreateConverter[ShieldGeneratorDeployable]() {
  override def ConstructorData(obj : ShieldGeneratorDeployable) : Try[AegisShieldGeneratorData] = {
    val health = 255 * obj.Health / obj.MaxHealth //TODO not precise
    if(health > 0) {
      Success(
        AegisShieldGeneratorData(
          CommonFieldData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = false,
            unk = 0,
            jammered = false,
            obj.Owner match {
              case Some(owner) => owner
              case None => PlanetSideGUID(0)
            }
          ),
          health
        )
      )
    }
    else {
      Success(
        AegisShieldGeneratorData(
          CommonFieldData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = true,
            unk = 0,
            jammered = false,
            player_guid = PlanetSideGUID(0)
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj : ShieldGeneratorDeployable) : Try[AegisShieldGeneratorData] =
    Failure(new Exception("converter should not be used to generate detailed ShieldGeneratorDdata"))
}
