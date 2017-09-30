// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.doors.{IFFLock, IFFLockDefinition}

/**
  * Wrapper `Class` designed to instantiate a `Door` server object.
  * @param idef a `IFFLockDefinition` object, indicating the specific functionality of the resulting `Door`
  * @param id the globally unique identifier to which this `IFFLock` will be registered
  */
class IFFLockObjectBuilder(private val idef : IFFLockDefinition, private val id : Int) extends ServerObjectBuilder[IFFLock] {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : IFFLock = {
    val obj = IFFLock()
    guid.register(obj, id) //non-Actor GUID registration
    obj
  }
}

object IFFLockObjectBuilder {
  /**
    * Overloaded constructor for a `IFFLockObjectBuilder`.
    * @param idef an `IFFLock` object
    * @param id a globally unique identifier
    * @return an `IFFLockObjectBuilder` object
    */
  def apply(idef : IFFLockDefinition, id : Int) : IFFLockObjectBuilder = {
    new IFFLockObjectBuilder(idef, id)
  }
}
