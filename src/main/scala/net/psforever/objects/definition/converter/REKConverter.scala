// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, SimpleItem}
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedREKData, REKData}

import scala.util.{Success, Try}

object REKConverter extends ObjectCreateConverter[SimpleItem] {
  override def ConstructorData(obj: SimpleItem): Try[REKData] = {
    Success(
      REKData(
        CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = true, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = Default.GUID0)
      )
    )
  }

  override def DetailedConstructorData(obj: SimpleItem): Try[DetailedREKData] = {
    Success(
      DetailedREKData(
        CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = true, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = Default.GUID0)
      )
    )
  }
}
