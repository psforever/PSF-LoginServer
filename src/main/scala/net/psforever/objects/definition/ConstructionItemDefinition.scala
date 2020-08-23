// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.avatar.Certification
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.definition.converter.ACEConverter
import net.psforever.objects.equipment.CItem

import scala.collection.mutable.ListBuffer

class ConstructionItemDefinition(objectId: Int) extends EquipmentDefinition(objectId) {
  CItem(objectId) //let throw NoSuchElementException
  private val modes: ListBuffer[ConstructionFireMode] = ListBuffer()
  Packet = new ACEConverter

  def Modes: ListBuffer[ConstructionFireMode] = modes
}

object ConstructionItemDefinition {
  def apply(objectId: Int): ConstructionItemDefinition = {
    new ConstructionItemDefinition(objectId)
  }

  def apply(cItem: CItem.Value): ConstructionItemDefinition = {
    new ConstructionItemDefinition(cItem.id)
  }
}

class ConstructionFireMode {
  private val deployables: ListBuffer[DeployedItem.Value] = ListBuffer.empty
  private val permissions: ListBuffer[Set[Certification]] = ListBuffer.empty

  def Permissions: ListBuffer[Set[Certification]] = permissions

  def Deployables: ListBuffer[DeployedItem.Value] = deployables

  def Item(deployable: DeployedItem.Value): ListBuffer[DeployedItem.Value] = {
    deployables += deployable
    permissions += Set.empty[Certification]
    deployables
  }

  def Item(deployable: DeployedItem.Value, permission: Set[Certification]): ListBuffer[DeployedItem.Value] = {
    deployables += deployable
    permissions += permission
    deployables
  }
}
