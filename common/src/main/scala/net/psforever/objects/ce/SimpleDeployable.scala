// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.SimpleDeployableDefinition

abstract class SimpleDeployable(cdef : SimpleDeployableDefinition) extends PlanetSideGameObject
  with Deployable {
  Health = Definition.MaxHealth

  def MaxHealth : Int = Definition.MaxHealth

  def Definition = cdef
}
