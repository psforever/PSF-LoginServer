// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedREKData, REKData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class REKConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj : SimpleItem) : Try[REKData] = {
    Success(
      REKData(
        CommonFieldData(
          PlanetSideEmpire.NEUTRAL, //TODO faction affinity
          false,
          false,
          true,
          None,
          false,
          Some(false),
          None,
          PlanetSideGUID(0))
      )
    )
  }

  override def DetailedConstructorData(obj : SimpleItem) : Try[DetailedREKData] = {
    Success(
      DetailedREKData(
        CommonFieldData(
          PlanetSideEmpire.NEUTRAL, //TODO faction affinity
          false,
          false,
          true,
          None,
          false,
          Some(false),
          None,
          PlanetSideGUID(0)
        )
      )
    )
  }
}
