// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity

/**
  * A structure-owned server object that is a "door lock."<br>
  * <br>
  * The "door lock" exerts an "identify friend or foe" field that detects the faction affiliation of a target player.
  * It also indirectly inherits faction affiliation from the structure to which it is connected
  * or it can be "hacked" whereupon the person exploiting it leaves their "faction" as the aforementioned affiliated faction.
  * The `IFFLock` is ideally associated with a server map object - a `Door` - to which it acts as a gatekeeper.
  * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class IFFLock(private val idef : IFFLockDefinition) extends Amenity with Hackable {
  def Definition : IFFLockDefinition = idef
}

object IFFLock {
  /**
    * Overloaded constructor.
    * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(idef : IFFLockDefinition) : IFFLock = {
    new IFFLock(idef)
  }

  import akka.actor.ActorContext
  /**
    * Instantiate an configure a `IFFLock` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `IFFLock` object
    */
  def Constructor(id : Int, context : ActorContext) : IFFLock = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = IFFLock(GlobalDefinitions.lock_external)
    obj.Actor = context.actorOf(Props(classOf[IFFLockControl], obj), s"${GlobalDefinitions.lock_external.Name}_$id")
    obj
  }
}
