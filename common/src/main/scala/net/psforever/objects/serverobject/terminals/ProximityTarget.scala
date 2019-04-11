// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

/**
  * A classification of the target of this terminal's interactions.
  * Arbitrary, but useful.
  */
object ProximityTarget extends Enumeration {
  val
  Aircraft,
  Equipment,
  Player,
  Vehicle
  = Value
}
