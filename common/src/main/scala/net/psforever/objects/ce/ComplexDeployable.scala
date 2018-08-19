// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject

abstract class ComplexDeployable(cdef : ObjectDefinition with LargeDeployableDefinition) extends PlanetSideServerObject
  with LargeDeployable {
  private var health : Int = 1
  Health = cdef.MaxHealth

  def Health : Int = health

  def Health_=(toHealth : Int) : Int = {
    health = toHealth
    Health
  }

  def MaxHealth : Int = Definition.MaxHealth

  def Definition = cdef
}

class ComplexDeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with LargeDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException

  def Item : DeployedItem.Value = item
}

object ComplexDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : ComplexDeployableDefinition = {
    new ComplexDeployableDefinition(dtype.id)
  }
}
