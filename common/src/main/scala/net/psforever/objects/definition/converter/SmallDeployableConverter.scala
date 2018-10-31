// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ce.Deployable
import net.psforever.objects.PlanetSideGameObject
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{PlacementData, SmallDeployableData}

import scala.util.{Failure, Success, Try}

class SmallDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with Deployable]() {
  override def ConstructorData(obj : PlanetSideGameObject with Deployable) : Try[SmallDeployableData] = {
    Success(
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
      )
    )
  }

  override def DetailedConstructorData(obj : PlanetSideGameObject with Deployable) : Try[SmallDeployableData] =
    Failure(new Exception("converter should not be used to generate detailed SmallDeployableData"))
}