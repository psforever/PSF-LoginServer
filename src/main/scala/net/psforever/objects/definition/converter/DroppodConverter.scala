// Copyright (c) 2020 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, Vehicle}
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

object DroppodConverter extends ObjectCreateConverter[Vehicle] {
  override def DetailedConstructorData(obj: Vehicle): Try[DroppodData] =
    Failure(new Exception("DroppodConverter should not be used to generate detailed DroppodData (nothing should)"))

  override def ConstructorData(obj: Vehicle): Try[DroppodData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 0) { //active
      Success(
        DroppodData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation, obj.Velocity),
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = None, jammered = obj.Jammed, v5 = None, GetOwner(obj))
          ),
          health,
          burn = false
        )
      )
    } else { //destroyed
      Success(
        DroppodData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation, obj.Velocity),
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = None, jammered = false, v5 = None, Default.GUID0)
          ),
          0,
          burn = false
        )
      )
    }
  }
}
