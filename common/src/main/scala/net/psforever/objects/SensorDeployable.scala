// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.SimpleDeployable
import net.psforever.objects.definition.SimpleDeployableDefinition
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.hackable.Hackable

class SensorDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef)
  with Hackable
  with JammableUnit
