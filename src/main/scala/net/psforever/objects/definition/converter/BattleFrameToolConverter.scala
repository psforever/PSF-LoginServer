// Copyright (c) 2021 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Tool
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataExtra, DetailedWeaponData, InternalSlot, WeaponData}
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class BattleFrameToolConverter extends ObjectCreateConverter[Tool]() {
  override def ConstructorData(obj: Tool): Try[WeaponData] = {
    val slots: List[InternalSlot] = (0 until obj.MaxAmmoSlot).map(index => {
      val box = obj.AmmoSlots(index).Box
      InternalSlot(box.Definition.ObjectId, box.GUID, index, box.Definition.Packet.ConstructorData(box).get)
    }).toList
    Success(
      WeaponData(
        CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = true, v2 = Some(CommonFieldDataExtra.Default), jammered = obj.Jammed, v5 = None, guid = PlanetSideGUID(0)),
        obj.FireModeIndex,
        slots
      )
    )
  }

  override def DetailedConstructorData(obj: Tool): Try[DetailedWeaponData] =
    Failure(new Exception("BattleFrameToolConverter should not be used to generate detailed BattleFrameRToolData (nothing should)"))
}
