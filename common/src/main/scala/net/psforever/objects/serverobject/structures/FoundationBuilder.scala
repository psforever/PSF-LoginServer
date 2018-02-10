// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.zones.Zone

/**
  * Defer establishment of a `Building` object until the location for the object is correct (in the correct zone)
  * and a `context` in the proper `Actor` hierarchy of that zone exists in scope.
  * @see `ServerObjectBuilder`
  * @see `Building`
  * @param constructor a curried function that eventually constructs a `Building` object
  */
class FoundationBuilder(private val constructor : (Int, Zone, ActorContext)=>Building) {
  def Build(id : Int, zone : Zone)(implicit context : ActorContext = null) : Building = {
    val obj : Building = constructor(id, zone, context)
    obj
  }
}

object FoundationBuilder {
  /**
    * Overloaded constructor.
    * @param constructor a curried function that eventually constructs a `Building` object
    * @return a `FoundationBuilder` object
    */
  def apply(constructor : (Int, Zone, ActorContext)=>Building) : FoundationBuilder = {
    new FoundationBuilder(constructor)
  }
}
