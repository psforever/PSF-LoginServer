// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.vital.VitalityDefinition

abstract class AmenityDefinition(objectId : Int) extends ObjectDefinition(objectId)
  with VitalityDefinition {
  Name = "amenity"
}
