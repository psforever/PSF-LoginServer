// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.builders

import akka.actor.Props
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}

/**
  * Wrapper `Class` designed to instantiate a `VehicleSpawnPad` server object.
  * @param spdef an `ObjectDefinition` object ...
  * @param id the globally unique identifier to which this `VehicleSpawnPad` will be registered
  */
class VehicleSpawnPadObjectBuilder(private val spdef : ObjectDefinition, private val id : Int) extends ServerObjectBuilder[VehicleSpawnPad] {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : VehicleSpawnPad = {
    val obj = VehicleSpawnPad(spdef)
    guid.register(obj, id) //non-Actor GUID registration
    obj.Actor = context.actorOf(Props(classOf[VehicleSpawnControl], obj), s"${spdef.Name}_${obj.GUID.guid}")
    obj
  }
}

object VehicleSpawnPadObjectBuilder {
  /**
    * Overloaded constructor for a `DoorObjectBuilder`.
    * @param spdef an `ObjectDefinition` object
    * @param id a globally unique identifier
    * @return a `VehicleSpawnPadObjectBuilder` object
    */
  def apply(spdef : ObjectDefinition, id : Int) : VehicleSpawnPadObjectBuilder = {
    new VehicleSpawnPadObjectBuilder(spdef, id)
  }
}
