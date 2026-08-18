// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{Default, SimpleItem}
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedCommandDetonaterData, HandheldData}

import scala.util.{Success, Try}

object CommandDetonaterConverter extends ObjectCreateConverter[SimpleItem] {
  override def ConstructorData(obj: SimpleItem): Try[HandheldData] = {
    Success(
      HandheldData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = None, jammered = false, v5 = None, guid = Default.GUID0)
      )
    )
  }

  override def DetailedConstructorData(obj: SimpleItem): Try[DetailedCommandDetonaterData] = {
    Success(
      DetailedCommandDetonaterData(
        CommonFieldData(obj.Faction, bops = false, alternate = false, v1 = false, v2 = None, jammered = false, v5 = None, guid = Default.GUID0)
      )
    )
  }
}
