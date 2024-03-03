// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.AmmoBox
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedAmmoBoxData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class AmmoBoxConverter extends ObjectCreateConverter[AmmoBox] {
  override def ConstructorData(obj: AmmoBox): Try[CommonFieldData] = {
    Success(CommonFieldData()(flag = false))
  }

  override def DetailedConstructorData(obj: AmmoBox): Try[DetailedAmmoBoxData] = {
    Success(
      DetailedAmmoBoxData(
        CommonFieldData(
          PlanetSideEmpire.NEUTRAL,
          bops = false,
          alternate = false,
          v1 = true,
          None,
          jammered = false,
          None,
          None,
          PlanetSideGUID(0)
        ),
        obj.Capacity
      )
    )
  }
}
