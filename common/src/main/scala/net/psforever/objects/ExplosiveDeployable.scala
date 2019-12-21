// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.SimpleDeployable
import net.psforever.objects.definition.SimpleDeployableDefinition
import net.psforever.objects.equipment.JammableUnit

class ExplosiveDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef)
  with JammableUnit {
  private var exploded : Boolean = false

  def Exploded : Boolean = exploded

  def Exploded_=(fuse : Boolean) : Boolean = {
    exploded = fuse
    Exploded
  }
}
