// Copyright (c) 2020 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class DroppodConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj: Vehicle): Try[DroppodData] =
    Failure(new Exception("DroppodConverter should not be used to generate detailed DroppodData (nothing should)"))

  override def ConstructorData(obj: Vehicle): Try[DroppodData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 0) { //active
      Success(
        DroppodData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation, obj.Velocity),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = false,
              v1 = false,
              v2 = None,
              jammered = obj.Jammed,
              v4 = Some(false),
              v5 = None,
              obj.Owner match {
                case Some(owner) => owner
                case None        => PlanetSideGUID(0)
              }
            )
          ),
          health,
          burn = false,
          unk = false
        )
      )
    } else { //destroyed
      Success(
        DroppodData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation, obj.Velocity),
            CommonFieldData(
              obj.Faction,
              bops = false,
              alternate = false,
              v1 = false,
              v2 = None,
              jammered = false,
              v4 = Some(false),
              v5 = None,
              PlanetSideGUID(0)
            )
          ),
          0,
          burn = false,
          unk = false
        )
      )
    }
  }
}
