// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.definition.ConstructionItemDefinition

class Telepad(private val cdef : ConstructionItemDefinition) extends ConstructionItem(cdef)
  with TelepadLike

object Telepad {
  def apply(cdef : ConstructionItemDefinition) : Telepad = {
    new Telepad(cdef)
  }
}