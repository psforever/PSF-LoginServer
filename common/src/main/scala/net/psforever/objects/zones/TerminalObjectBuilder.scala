// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.terminals.{Terminal, TerminalDefinition}

/**
  * Wrapper `Class` designed to instantiate a `Terminal` server object.
  * @param tdef a `TerminalDefinition` object, indicating the specific functionality of the resulting `Terminal`
  * @param id the globally unique identifier to which this `Terminal` will be registered
  */
class TerminalObjectBuilder(private val tdef : TerminalDefinition, private val id : Int) extends ServerObjectBuilder {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : Terminal = {
    val obj = Terminal(tdef)
    guid.register(obj, id) //non-Actor GUID registration
    obj.Actor //it's necessary to register beforehand because the Actor name utilizes the GUID
    obj
  }
}

object TerminalObjectBuilder {
  /**
    * Overloaded constructor for a `TerminalObjectBuilder`.
    * @param tdef a `TerminalDefinition` object
    * @param id a globally unique identifier
    * @return a `TerminalObjectBuilder` object
    */
  def apply(tdef : TerminalDefinition, id : Int) : TerminalObjectBuilder = {
    new TerminalObjectBuilder(tdef, id)
  }
}
