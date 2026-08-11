// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.objectcreate.{CommonFieldData, CommonFieldDataExtra}
import net.psforever.types.PlanetSideGUID

import scala.util.{Success, Try}

class TerminalConverter extends ObjectCreateConverter[Terminal]() {
  override def ConstructorData(obj: Terminal): Try[CommonFieldData] = {
    Success(
      CommonFieldData(faction = obj.Faction, bops = false, alternate = false, v1 = false, v2 = Some(CommonFieldDataExtra.Default), jammered = false, v5 = None, guid = PlanetSideGUID(0))
    )
  }
}
