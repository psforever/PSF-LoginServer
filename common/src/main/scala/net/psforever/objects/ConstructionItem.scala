// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ConstructionItemDefinition
import net.psforever.objects.equipment.{CItem, Equipment, FireModeSwitch}

class ConstructionItem(private val cItemDef : ConstructionItemDefinition) extends Equipment with FireModeSwitch[CItem.DeployedItem.Value] {
  private var fireModeIndex : Int = 0

  def FireModeIndex : Int = fireModeIndex

  def FireModeIndex_=(index : Int) : Int = {
    fireModeIndex = index % cItemDef.Modes.length
    FireModeIndex
  }

  def FireMode : CItem.DeployedItem.Value = cItemDef.Modes(fireModeIndex)

  def NextFireMode : CItem.DeployedItem.Value = {
    FireModeIndex = FireModeIndex + 1
    FireMode
  }

  def Definition : ConstructionItemDefinition = cItemDef
}

object ConstructionItem {
  def apply(cItemDef : ConstructionItemDefinition) : ConstructionItem = {
    new ConstructionItem(cItemDef)
  }
}
