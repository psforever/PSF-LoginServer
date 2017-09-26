// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.doors.{Door, DoorDefinition}

/**
  * Wrapper `Class` designed to instantiate a `Door` server object.
  * @param ddef a `DoorDefinition` object, indicating the specific functionality of the resulting `Door`
  * @param id the globally unique identifier to which this `Door` will be registered
  */
class DoorObjectBuilder(private val ddef : DoorDefinition, private val id : Int) extends ServerObjectBuilder[Door] {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : Door = {
    val obj = Door(ddef)
    guid.register(obj, id) //non-Actor GUID registration
    obj.Actor //it's necessary to register beforehand because the Actor name utilizes the GUID
    obj
  }
}

object DoorObjectBuilder {
  /**
    * Overloaded constructor for a `DoorObjectBuilder`.
    * @param ddef a `DoorDefinition` object
    * @param id a globally unique identifier
    * @return a `DoorObjectBuilder` object
    */
  def apply(ddef : DoorDefinition, id : Int) : DoorObjectBuilder = {
    new DoorObjectBuilder(ddef, id)
  }
}
