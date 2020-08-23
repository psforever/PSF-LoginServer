package net.psforever.objects.serverobject.structures

import net.psforever.objects.{NtuContainerDefinition, SpawnPointDefinition}
import net.psforever.objects.definition.ObjectDefinition

class BuildingDefinition(objectId: Int) extends ObjectDefinition(objectId) with NtuContainerDefinition with SphereOfInfluence {
  Name = "building"
  MaxNtuCapacitor = Int.MaxValue
}

class WarpGateDefinition(objectId: Int) extends BuildingDefinition(objectId) with SpawnPointDefinition
