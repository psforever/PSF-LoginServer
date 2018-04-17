// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

/**
  * An `Enumeration` of the kinds of building structures found in the game.
  * This is merely a kludge for more a future, more complicated internal object that handles base operations.
  */
object StructureType extends Enumeration {
  type Type = Value

  val
  Bridge,
  Building, //generic
  Bunker,
  Facility,
  Platform, //outdoor amenities like the spawn pads in sanctuary
  Tower,
  WarpGate
  = Value
}
