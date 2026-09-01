// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{ConstructionItem, Default}
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedConstructionToolData, HandheldData}

import scala.util.{Success, Try}

object ACEConverter extends ObjectCreateConverter[ConstructionItem] {
  override def ConstructorData(obj: ConstructionItem): Try[HandheldData] = {
    Success(
      HandheldData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = None, guid = Default.GUID0),
        obj.FireModeIndex
      )
    )
  }

  override def DetailedConstructorData(obj: ConstructionItem): Try[DetailedConstructionToolData] = {
    Success(
      DetailedConstructionToolData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = true, v2 = None, jammered = false, v5 = None, guid = Default.GUID0),
        obj.FireModeIndex
      )
    )
  }
}
