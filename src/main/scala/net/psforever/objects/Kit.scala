// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.KitDefinition
import net.psforever.objects.equipment.Equipment

/**
  * A one-time-use recovery item that can be applied by the player while held within their inventory.
  * @param kitDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class Kit(private val kitDef: KitDefinition) extends Equipment {
  def Definition: KitDefinition = kitDef
}

object Kit {
  def apply(kitDef: KitDefinition): Kit = {
    new Kit(kitDef)
  }
}
