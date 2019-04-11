// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

/**
  * An `Enumeration` of the kinds of building structures found in the game.
  * This is merely a kludge for more a future, more complicated internal object that handles base operations.
  */
object StructureType extends Enumeration {
  type Type = Value

  val
  Bridge, //technically, a "bridge section"
  Building, //generic
  Bunker, //low accessible ground cover
  Facility, //large base
  Platform, //outdoor amenities disconnected froma proper base like the vehicle spawn pads in sanctuary
  Tower, //also called field towers: watchtower, air tower, gun tower
  WarpGate //transport point between zones
  = Value
}
