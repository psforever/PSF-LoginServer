// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, TrapDeployable}
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

object TRAPConverter extends ObjectCreateConverter[TrapDeployable] {
  override def ConstructorData(obj: TrapDeployable): Try[TRAPData] = {
    val health = StatConverter.Health(obj.Health, obj.MaxHealth)
    if (health > 0) {
      Success(
        TRAPData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, None, jammered = false, None, GetOwner(obj))
          ),
          health
        )
      )
    } else {
      Success(
        TRAPData(
          CommonFieldDataWithPlacement(
            PlacementData(obj.Position, obj.Orientation),
            CommonFieldData(obj.Faction, bops = false, alternate = true, v1 = true, None, jammered = false, None, Default.GUID0)
          ),
          0
        )
      )
    }
  }

  override def DetailedConstructorData(obj: TrapDeployable): Try[TRAPData] =
    Failure(new Exception("converter should not be used to generate detailed TRAPData"))
}
