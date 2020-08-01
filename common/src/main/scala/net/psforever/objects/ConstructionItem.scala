// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.avatar.Certification
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.definition.{ConstructionFireMode, ConstructionItemDefinition}
import net.psforever.objects.equipment.{Equipment, FireModeSwitch}

/**
  * A type of `Equipment` that can be wielded and applied to the game world to produce other game objects.<br>
  * <br>
  * Functionally, `ConstructionItem` objects resemble `Tool` objects that have fire mode state and alternate "ammunition."
  * Very much unlike `Tool` object counterparts, however,
  * the alternate "ammunition" is also a type of fire mode state
  * maintained in a two-dimensional grid of related states.
  * These states represent output products called deployables or, in the common vernacular, CE.
  * Also unlike `Tool` objects, whose ammunition is always available even when drawing the weapon is not permitted,
  * the different states are not all available if just the equipment itself is available.
  * Parameters along with these CE states
  * indicate whether the current output product is something the player is permitted to utilize.
  * @param cItemDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class ConstructionItem(private val cItemDef: ConstructionItemDefinition)
    extends Equipment
    with FireModeSwitch[ConstructionFireMode] {
  private var fireModeIndex: Int = 0
  private var ammoTypeIndex: Int = 0

  def FireModeIndex: Int = fireModeIndex

  def FireModeIndex_=(index: Int): Int = {
    fireModeIndex = index % Definition.Modes.length
    FireModeIndex
  }

  def FireMode: ConstructionFireMode = Definition.Modes(fireModeIndex)

  def NextFireMode: ConstructionFireMode = {
    FireModeIndex = FireModeIndex + 1
    ammoTypeIndex = 0
    FireMode
  }

  def AmmoTypeIndex: Int = ammoTypeIndex

  def AmmoTypeIndex_=(index: Int): Int = {
    ammoTypeIndex = index % FireMode.Deployables.length
    AmmoTypeIndex
  }

  def AmmoType: DeployedItem.Value = FireMode.Deployables(ammoTypeIndex)

  def NextAmmoType: DeployedItem.Value = {
    AmmoTypeIndex = AmmoTypeIndex + 1
    FireMode.Deployables(ammoTypeIndex)
  }

  def ModePermissions: Set[Certification] = FireMode.Permissions(ammoTypeIndex)

  def Definition: ConstructionItemDefinition = cItemDef
}

object ConstructionItem {
  def apply(cItemDef: ConstructionItemDefinition): ConstructionItem = {
    new ConstructionItem(cItemDef)
  }
}
