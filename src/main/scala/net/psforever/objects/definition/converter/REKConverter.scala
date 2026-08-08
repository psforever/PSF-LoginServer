// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedREKData, REKData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Success, Try}

class REKConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj: SimpleItem): Try[REKData] = {
    Success(
      REKData(
        CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = true, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = PlanetSideGUID(0))
      )
    )
  }

  override def DetailedConstructorData(obj: SimpleItem): Try[DetailedREKData] = {
    Success(
      DetailedREKData(
        CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = true, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = PlanetSideGUID(0))
      )
    )
  }
}
