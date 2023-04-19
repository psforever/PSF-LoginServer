// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The definition for any `Door`.
  */
class DoorDefinition(objectId: Int)
  extends AmenityDefinition(objectId) {
  Name = "door"
  /** range wherein the door may first be opened
   * (note: intentionally inflated as the initial check on the client occurs further than expected) */
  var initialOpeningDistance: Float = 7.5f
  /** range within which the door must detect a target player to remain open */
  var continuousOpenDistance: Float = 5.05f
}
