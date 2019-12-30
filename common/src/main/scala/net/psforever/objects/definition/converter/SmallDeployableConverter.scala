// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ce.Deployable
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.equipment.JammableUnit
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class SmallDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with Deployable]() {
  override def ConstructorData(obj : PlanetSideGameObject with Deployable) : Try[CommonFieldDataWithPlacement] = {
    Success(
      CommonFieldDataWithPlacement(
        PlacementData(obj.Position, obj.Orientation),
        CommonFieldData(
          obj.Faction,
          bops = false,
          alternate = false,
          false,
          None,
          jammered = obj match {
            case o : JammableUnit => o.Jammed
            case _ => false
          },
          Some(false),
          None,
          obj.Owner match {
            case Some(owner) => owner
            case None => PlanetSideGUID(0)
          }
        )
      )
    )
  }

  override def DetailedConstructorData(obj : PlanetSideGameObject with Deployable) : Try[CommonFieldDataWithPlacement] =
    Failure(new Exception("converter should not be used to generate detailed small deployable data"))
}
