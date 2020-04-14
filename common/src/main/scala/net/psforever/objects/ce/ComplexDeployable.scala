// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.definition.ComplexDeployableDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject

abstract class ComplexDeployable(cdef : ComplexDeployableDefinition) extends PlanetSideServerObject
  with Deployable {
  private var shields : Int = 0

  def Shields : Int = shields

  def Shields_=(toShields : Int) : Int = {
    shields = math.min(math.max(0, toShields), MaxShields)
    Shields
  }

  def MaxShields : Int = {
    0//Definition.MaxShields
  }

  def Definition = cdef
}
