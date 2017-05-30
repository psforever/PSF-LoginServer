// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.equipment.CItem

import scala.collection.mutable.ListBuffer

class ConstructionItemDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  CItem.Unit(objectId) //let throw NoSuchElementException
  private val modes : ListBuffer[CItem.DeployedItem.Value] = ListBuffer()

  def Modes : ListBuffer[CItem.DeployedItem.Value] = modes
}

object ConstructionItemDefinition {
  def apply(objectId : Int) : ConstructionItemDefinition = {
    new ConstructionItemDefinition(objectId)
  }

  def apply(cItem : CItem.Unit.Value) : ConstructionItemDefinition = {
    new ConstructionItemDefinition(cItem.id)
  }
}
