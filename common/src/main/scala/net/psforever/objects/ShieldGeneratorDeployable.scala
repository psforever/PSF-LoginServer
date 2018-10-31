// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.{ComplexDeployable, DeployableCategory}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.ShieldGeneratorConverter
import net.psforever.objects.serverobject.hackable.Hackable

class ShieldGeneratorDeployable(cdef : ShieldGeneratorDefinition) extends ComplexDeployable(cdef)
  with Hackable

class ShieldGeneratorDefinition extends DeployableDefinition(240) {
  Packet = new ShieldGeneratorConverter
  DeployCategory = DeployableCategory.ShieldGenerators
}
