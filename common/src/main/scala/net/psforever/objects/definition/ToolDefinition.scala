// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.ToolConverter
import net.psforever.objects.equipment.FireModeDefinition

import scala.collection.mutable

class ToolDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  private val ammoTypes : mutable.ListBuffer[AmmoBoxDefinition] = new mutable.ListBuffer[AmmoBoxDefinition]
  private val fireModes : mutable.ListBuffer[FireModeDefinition] = new mutable.ListBuffer[FireModeDefinition]
  Name = "tool"
  Packet = new ToolConverter()

  def AmmoTypes : mutable.ListBuffer[AmmoBoxDefinition] = ammoTypes

  def FireModes : mutable.ListBuffer[FireModeDefinition] = fireModes
}

object ToolDefinition {
  def apply(objectId : Int) : ToolDefinition = {
    new ToolDefinition(objectId)
  }
}
