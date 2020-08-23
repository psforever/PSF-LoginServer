// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.resourcesilo

import net.psforever.objects.NtuContainerDefinition
import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The definition for any `Resource Silo`.
  * Object Id 731.
  */
class ResourceSiloDefinition extends AmenityDefinition(731)
  with NtuContainerDefinition {
  Name = "resource_silo"
  MaxNtuCapacitor = 1000
}
