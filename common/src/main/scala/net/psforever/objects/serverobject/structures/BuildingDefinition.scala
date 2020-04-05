package net.psforever.objects.serverobject.structures

import net.psforever.objects.SpawnPointDefinition
import net.psforever.objects.definition.ObjectDefinition

class BuildingDefinition(objectId : Int) extends ObjectDefinition(objectId)
  with SphereOfInfluence {
  Name = "building"
}

class WarpGateDefinition(objectId : Int) extends BuildingDefinition(objectId)
  with SpawnPointDefinition
