// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedConstructionToolData, HandheldData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class BoomerTriggerConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj: SimpleItem): Try[HandheldData] = {
    Success(HandheldData(CommonFieldData()))
  }

  override def DetailedConstructorData(obj: SimpleItem): Try[DetailedConstructionToolData] = {
    Success(
      DetailedConstructionToolData(
        CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0))
      )
    )
  }
}
