// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.TelepadDeployable
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class TelepadDeployableConverter extends ObjectCreateConverter[TelepadDeployable]() {
  override def ConstructorData(obj : TelepadDeployable) : Try[DroppedItemData[TelepadDeployableData]] = {
    obj.Router match {
      case Some(PlanetSideGUID(0)) =>
        Failure(new IllegalStateException("TelepadDeployableConverter: knowledge of associated Router is null"))

      case Some(router) =>
        if(obj.Health > 0) {
          Success(
            DroppedItemData(
              PlacementData(obj.Position, obj.Orientation),
              TelepadDeployableData(
                CommonFieldData(
                  obj.Faction,
                  bops = false,
                  alternate = false,
                  true,
                  None,
                  false,
                  None,
                  Some(router.guid),
                  obj.Owner.getOrElse(PlanetSideGUID(0))
                ),
                unk1 = 87,
                unk2 = 12
              )
            )
          )
        }
        else {
          Success(
            DroppedItemData(
              PlacementData(obj.Position, obj.Orientation),
              TelepadDeployableData(
                CommonFieldData(
                  obj.Faction,
                  bops = false,
                  alternate = true,
                  true,
                  None,
                  false,
                  None,
                  Some(router.guid),
                  PlanetSideGUID(0)
                ),
                unk1 = 0,
                unk2 = 6
              )
            )
          )
        }

      case None =>
        Failure(new IllegalStateException("TelepadDeployableConverter: telepad needs to know id of its associated Router"))
    }
  }
}
