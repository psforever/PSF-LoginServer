// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Kit
import net.psforever.packet.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedAmmoBoxData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class KitConverter extends ObjectCreateConverter[Kit]() {
  override def ConstructorData(obj: Kit): Try[CommonFieldData] = {
    Success(
      CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = false, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = PlanetSideGUID(0))
    )
  }

  override def DetailedConstructorData(obj: Kit): Try[DetailedAmmoBoxData] = {
    Success(DetailedAmmoBoxData(
      CommonFieldData(PlanetSideEmpire.NEUTRAL, bops = false, alternate = false, v1 = true, None, jammered = false, None, PlanetSideGUID(0)),
      1
    ))
  }
}
