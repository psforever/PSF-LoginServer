// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedCommandDetonaterData, HandheldData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class CommandDetonaterConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj : SimpleItem) : Try[HandheldData] = {
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
          None,
          PlanetSideGUID(0)
        )
      )
    )
  }

  override def DetailedConstructorData(obj : SimpleItem) : Try[DetailedCommandDetonaterData] = {
    Success(
      DetailedCommandDetonaterData(
        CommonFieldData(
          obj.Faction,
          false,
          false,
          false,
          None,
          false,
          None,
          None,
          PlanetSideGUID(0)
        )
      )
    )
  }
}
