// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.AmmoBox
import net.psforever.packet.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedAmmoBoxData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Success, Try}

class AmmoBoxConverter extends ObjectCreateConverter[AmmoBox] {
  override def ConstructorData(obj: AmmoBox): Try[CommonFieldData] = {
    Success(
      CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = Some(CommonFieldDataExtra.Default), jammered = false, None, PlanetSideGUID(0))
    )
  }

  override def DetailedConstructorData(obj: AmmoBox): Try[DetailedAmmoBoxData] = {
    Success(
      DetailedAmmoBoxData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, None, PlanetSideGUID(0)),
        obj.Capacity
      )
    )
  }
}
