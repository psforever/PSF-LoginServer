// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.TelepadDeployable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class TelepadDeployableConverter extends ObjectCreateConverter[TelepadDeployable]() {
  override def ConstructorData(obj : TelepadDeployable) : Try[TelepadDeployableData] = {
    if(obj.Router.isEmpty || obj.Router.contains(PlanetSideGUID(0))) {
      Failure(new IllegalStateException("TelepadDeployableConverter: telepad deployable needs to know id of its router"))
    }
    else {
      if(obj.Health > 0) {
        Success(
          TelepadDeployableData(
            CommonFieldDataWithPlacement(
              PlacementData(obj.Position, obj.Orientation),
              CommonFieldData(
                obj.Faction,
                bops = false,
                alternate = false,
                true,
                None,
                false,
                None,
                Some(obj.Router.get.guid),
                obj.Owner.getOrElse(PlanetSideGUID(0))
              )
            ),
            unk1 = 87,
            unk2 = 12
          )
        )
      }
      else {
        Success(
          TelepadDeployableData(
            CommonFieldDataWithPlacement(
              PlacementData(obj.Position, obj.Orientation),
              CommonFieldData(
                obj.Faction,
                bops = false,
                alternate = true,
                true,
                None,
                false,
                None,
                Some(obj.Router.get.guid),
                PlanetSideGUID(0)
              )
            ),
            unk1 = 0,
            unk2 = 6
          )
        )
      }
    }
  }
}
