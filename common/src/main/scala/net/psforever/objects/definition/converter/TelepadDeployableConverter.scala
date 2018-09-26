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
        Success(TelepadDeployableData(
          PlacementData(obj.Position, obj.Orientation),
          obj.Faction,
          bops = false,
          destroyed = false,
          unk1 = 2,
          unk2 = true,
          obj.Router.get,
          obj.Owner.getOrElse(PlanetSideGUID(0)),
          unk3 = 87,
          unk4 = 12
        ))
      }
      else {
        Success(TelepadDeployableData(
          PlacementData(obj.Position, obj.Orientation),
          obj.Faction,
          bops = false,
          destroyed = true,
          unk1 = 2,
          unk2 = true,
          obj.Router.get,
          owner_guid = PlanetSideGUID(0),
          unk3 = 0,
          unk4 = 6
        ))
      }
    }
  }
}
