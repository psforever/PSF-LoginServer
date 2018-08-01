// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.ToolConverter
import net.psforever.objects.equipment.FireModeDefinition

import scala.collection.mutable

class ToolDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  private val ammoTypes : mutable.ListBuffer[AmmoBoxDefinition] = new mutable.ListBuffer[AmmoBoxDefinition]
  private val projectileTypes : mutable.ListBuffer[ProjectileDefinition] = new mutable.ListBuffer[ProjectileDefinition]
  private val fireModes : mutable.ListBuffer[FireModeDefinition] = new mutable.ListBuffer[FireModeDefinition]
  Name = "tool"
  Packet = ToolDefinition.converter

  def AmmoTypes : mutable.ListBuffer[AmmoBoxDefinition] = ammoTypes

  def ProjectileTypes : mutable.ListBuffer[ProjectileDefinition] = projectileTypes

  def FireModes : mutable.ListBuffer[FireModeDefinition] = fireModes

  def NextFireModeIndex(index : Int) : Int = index + 1
}

object ToolDefinition {
  private val converter = new ToolConverter()

  def apply(objectId : Int) : ToolDefinition = {
    new ToolDefinition(objectId)
  }
}
