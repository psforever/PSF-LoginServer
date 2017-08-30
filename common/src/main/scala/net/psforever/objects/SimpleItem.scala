// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.SimpleItemDefinition
import net.psforever.objects.equipment.Equipment

class SimpleItem(private val simpDef : SimpleItemDefinition) extends Equipment {
  def Definition : SimpleItemDefinition = simpDef
}

object SimpleItem {
  def apply(simpDef : SimpleItemDefinition) : SimpleItem = {
    new SimpleItem(simpDef)
  }

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID, simpDef : SimpleItemDefinition) : SimpleItem = {
    val obj = new SimpleItem(simpDef)
    obj.GUID = guid
    obj
  }
}
