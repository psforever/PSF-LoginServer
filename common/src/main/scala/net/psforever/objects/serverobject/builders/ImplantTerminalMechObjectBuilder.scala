// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.builders

import akka.actor.Props
import net.psforever.objects.serverobject.implantmech.{ImplantTerminalMech, ImplantTerminalMechControl, ImplantTerminalMechDefinition}

/**
  * Wrapper `Class` designed to instantiate a `ImplantTerminalMech` server object.
  * @param idef a `ImplantTerminalMechDefinition` object, indicating the specific functionality of the resulting `Door`
  * @param id the globally unique identifier to which this "tube" will be registered
  */
class ImplantTerminalMechObjectBuilder(private val idef : ImplantTerminalMechDefinition, private val id : Int) extends ServerObjectBuilder[ImplantTerminalMech] {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : ImplantTerminalMech = {
    val obj = ImplantTerminalMech(idef)
    guid.register(obj, id) //non-Actor GUID registration
    obj.Actor = context.actorOf(Props(classOf[ImplantTerminalMechControl], obj), s"${idef.Name}_${obj.GUID.guid}")
    obj
  }
}

object ImplantTerminalMechObjectBuilder {
  /**
    * Overloaded constructor for a `DoorObjectBuilder`.
    * @param idef a `DoorDefinition` object
    * @param id a globally unique identifier
    * @return a `DoorObjectBuilder` object
    */
  def apply(idef : ImplantTerminalMechDefinition, id : Int) : ImplantTerminalMechObjectBuilder = {
    new ImplantTerminalMechObjectBuilder(idef, id)
  }
}
