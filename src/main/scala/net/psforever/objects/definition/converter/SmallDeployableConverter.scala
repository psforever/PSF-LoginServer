// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ce.Deployable
import net.psforever.objects.equipment.JammableUnit
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class SmallDeployableConverter extends ObjectCreateConverter[Deployable]() {
  override def ConstructorData(obj: Deployable): Try[SmallDeployableData] = {
    Success(
      SmallDeployableData(CommonFieldDataWithPlacement(
        PlacementData(obj.Position, obj.Orientation),
        CommonFieldData(
          obj.Faction,
          bops = false,
          alternate = obj.Destroyed,
          v1 = false,
          None,
          jammered = obj match {
            case o: JammableUnit => o.Jammed
            case _               => false
          },
          Some(false),
          None,
          obj.OwnerGuid match {
            case Some(owner) => owner
            case None        => PlanetSideGUID(0)
          }
        )
      ))
    )
  }

  override def DetailedConstructorData(obj: Deployable): Try[SmallDeployableData] =
    Failure(new Exception("converter should not be used to generate detailed small deployable data"))
}
