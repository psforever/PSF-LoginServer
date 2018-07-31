// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.TrapDeployable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class TRAPConverter extends ObjectCreateConverter[TrapDeployable]() {
  override def ConstructorData(obj : TrapDeployable) : Try[TRAPData] = {
    val health = 255 * obj.Health / obj.MaxHealth //TODO not precise
    if(health > 3) {
      Success(
        TRAPData(
          SmallDeployableData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = false,
            unk1 = 0,
            jammered = false,
            unk2 = false,
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
        TRAPData(
          SmallDeployableData(
            PlacementData(obj.Position, obj.Orientation),
            obj.Faction,
            bops = false,
            destroyed = true,
            unk1 = 0,
            jammered = false,
            unk2 = false,
            owner_guid = PlanetSideGUID(0)
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj : TrapDeployable) : Try[TRAPData] =
    Failure(new Exception("converter should not be used to generate detailed TRAPData"))
}
