// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{AmmoBox, Default}
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedAmmoBoxData}

import scala.util.{Success, Try}

object AmmoBoxConverter extends ObjectCreateConverter[AmmoBox] {
  override def ConstructorData(obj: AmmoBox): Try[CommonFieldData] = {
    Success(
      CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = Some(CommonFieldDataExtra.Default), jammered = false, None, Default.GUID0)
    )
  }

  override def DetailedConstructorData(obj: AmmoBox): Try[DetailedAmmoBoxData] = {
    Success(
      DetailedAmmoBoxData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, None, Default.GUID0),
        obj.Capacity
      )
    )
  }
}
