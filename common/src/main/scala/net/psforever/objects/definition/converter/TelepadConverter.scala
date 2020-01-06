// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Telepad
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedConstructionToolData, HandheldData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class TelepadConverter extends ObjectCreateConverter[Telepad]() {
  override def ConstructorData(obj : Telepad) : Try[HandheldData] = {
    obj.Router match {
      case Some(router) =>
        Success(
          HandheldData(
            CommonFieldData(
              obj.Faction,
              false,
              false,
              false,
              None,
              false,
              None,
              Some(router.guid),
              PlanetSideGUID(0)
            )
          )
        )
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }

  override def DetailedConstructorData(obj : Telepad) : Try[DetailedConstructionToolData] = {
    obj.Router match {
      case Some(router) =>
        Success(
          DetailedConstructionToolData(
            CommonFieldData(
              obj.Faction,
              false,
              false,
              true,
              None,
              false,
              None,
              Some(router.guid),
              PlanetSideGUID(0)
            )
          )
        )
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }
}
