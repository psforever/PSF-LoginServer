// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.definition.ToolDefinition

import scala.collection.mutable

trait MountedWeaponsDefinition {
  /* key - mount index (where this weapon attaches during object construction), value - the weapon on an EquipmentSlot */
  protected var weapons: mutable.HashMap[Int, ToolDefinition]      = mutable.HashMap[Int, ToolDefinition]()

  def Weapons: mutable.HashMap[Int, ToolDefinition] = weapons
}
