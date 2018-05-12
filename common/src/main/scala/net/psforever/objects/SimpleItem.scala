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
}
