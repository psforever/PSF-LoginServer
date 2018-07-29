// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.equipment.CItem

class DeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId) {
  private val item = CItem.DeployedItem(objectId) //let throw NoSuchElementException
  Name = "deployable"

  def Item : CItem.DeployedItem.Value = item
}

object DeployableDefinition {
  def apply(dtype : CItem.DeployedItem.Value) : DeployableDefinition = {
    new DeployableDefinition(dtype.id)
  }
}
