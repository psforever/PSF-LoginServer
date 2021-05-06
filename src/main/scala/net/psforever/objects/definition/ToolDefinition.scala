// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.ToolConverter
import net.psforever.objects.equipment.FireModeDefinition

import scala.collection.mutable

class ToolDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  private val ammoTypes: mutable.ListBuffer[AmmoBoxDefinition]          = new mutable.ListBuffer[AmmoBoxDefinition]
  private val projectileTypes: mutable.ListBuffer[ProjectileDefinition] = new mutable.ListBuffer[ProjectileDefinition]
  private val fireModes: mutable.ListBuffer[FireModeDefinition]         = new mutable.ListBuffer[FireModeDefinition]
  /**
    * the multiplier value at which the base repair amount of an ammunition operates;
    * by default, it always has a single "no repair" multiplier
    */
  private val repairMultipliers: mutable.ListBuffer[Float]              = new mutable.ListBuffer[Float].addOne(0f)
  private var defaultFireModeIndex: Option[Int]                         = None
  Name = "tool"
  Packet = ToolDefinition.converter

  def AmmoTypes: mutable.ListBuffer[AmmoBoxDefinition] = ammoTypes

  def ProjectileTypes: mutable.ListBuffer[ProjectileDefinition] = projectileTypes

  def FireModes: mutable.ListBuffer[FireModeDefinition] = fireModes

  def NextFireModeIndex(index: Int): Int = index + 1

  def RepairMultipliers: Seq[Float] = repairMultipliers.toSeq

  def RepairMultiplier(level: Int): Float = {
    if (level > -1 && level < repairMultipliers.size) {
      repairMultipliers(level)
    } else {
      0f
    }
  }

  def AddRepairMultiplier(level: Int, value: Float): Seq[Float] = {
    if (level > 0) {
      while(repairMultipliers.size <= level) {
        repairMultipliers.addOne(0f)
      }
      repairMultipliers.update(level, value)
    }
    RepairMultipliers
  }

  def DefaultFireModeIndex: Int = defaultFireModeIndex.getOrElse(0)

  def DefaultFireModeIndex_=(index: Int): Int = DefaultFireModeIndex_=(Some(index))

  def DefaultFireModeIndex_=(index: Option[Int]): Int = {
    defaultFireModeIndex = index
    DefaultFireModeIndex
  }
}

object ToolDefinition {
  private val converter = new ToolConverter()

  def apply(objectId: Int): ToolDefinition = {
    new ToolDefinition(objectId)
  }
}
