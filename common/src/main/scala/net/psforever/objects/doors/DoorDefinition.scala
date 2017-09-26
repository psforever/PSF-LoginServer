// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import net.psforever.objects.definition.ObjectDefinition

/**
  * The definition for any `door`.
  * @param objectId the object's identifier number
  */
abstract class DoorDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  Name = "door"
}

