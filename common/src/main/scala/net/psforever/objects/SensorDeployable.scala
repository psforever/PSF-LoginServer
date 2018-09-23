// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.SimpleDeployable
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.serverobject.hackable.Hackable

class SensorDeployable(cdef : DeployableDefinition) extends SimpleDeployable(cdef)
  with Hackable
