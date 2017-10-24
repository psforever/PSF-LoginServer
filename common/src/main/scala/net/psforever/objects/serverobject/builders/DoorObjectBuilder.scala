// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.builders

import akka.actor.Props
import net.psforever.objects.serverobject.doors.{Door, DoorControl, DoorDefinition}

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
    obj.Actor = context.actorOf(Props(classOf[DoorControl], obj), s"${ddef.Name}_${obj.GUID.guid}")
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
