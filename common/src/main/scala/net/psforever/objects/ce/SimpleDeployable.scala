// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.DeployableDefinition

abstract class SimpleDeployable(cdef : DeployableDefinition) extends PlanetSideGameObject
  with Deployable {
  Health = Definition.MaxHealth

  def MaxHealth : Int = Definition.MaxHealth

  def Definition = cdef
}
