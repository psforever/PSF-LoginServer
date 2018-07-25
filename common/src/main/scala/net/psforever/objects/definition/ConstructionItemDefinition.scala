// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.ACEConverter
import net.psforever.objects.equipment.CItem
import net.psforever.types.CertificationType

import scala.collection.mutable.ListBuffer

class ConstructionItemDefinition(objectId : Int) extends EquipmentDefinition(objectId) {
  CItem.Unit(objectId) //let throw NoSuchElementException
  private val modes : ListBuffer[ConstructionFireMode] = ListBuffer()
  Packet = new ACEConverter

  def Modes : ListBuffer[ConstructionFireMode] = modes
}

object ConstructionItemDefinition {
  def apply(objectId : Int) : ConstructionItemDefinition = {
    new ConstructionItemDefinition(objectId)
  }

  def apply(cItem : CItem.Unit.Value) : ConstructionItemDefinition = {
    new ConstructionItemDefinition(cItem.id)
  }
}

class ConstructionFireMode {
  private val deployables : ListBuffer[CItem.DeployedItem.Value] = ListBuffer.empty
  private val permissions : ListBuffer[Set[CertificationType.Value]] = ListBuffer.empty

  def Permissions : ListBuffer[Set[CertificationType.Value]] = permissions

  def Deployables : ListBuffer[CItem.DeployedItem.Value] = deployables

  def Item(deployable : CItem.DeployedItem.Value) : ListBuffer[CItem.DeployedItem.Value] = {
    deployables += deployable
    permissions += Set.empty[CertificationType.Value]
    deployables
  }

  def Item(deployPair : (CItem.DeployedItem.Value, Set[CertificationType.Value])) : ListBuffer[CItem.DeployedItem.Value] = {
    val (deployable, permission) = deployPair
    deployables += deployable
    permissions += permission
    deployables
  }
}
