// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{DeployableDefinition, ObjectDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter

abstract class SimpleDeployable(cdef : SimpleDeployableDefinition) extends PlanetSideGameObject
  with Deployable {

  def Definition = cdef
}

class SimpleDeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with DeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  Packet = new SmallDeployableConverter

  def Item : DeployedItem.Value = item
}

object SimpleDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : SimpleDeployableDefinition = {
    new SimpleDeployableDefinition(dtype.id)
  }
}
