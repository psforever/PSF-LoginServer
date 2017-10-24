// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.builders

import akka.actor.Props
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl, TerminalDefinition}

/**
  * Wrapper `Class` designed to instantiate a `Terminal` server object.
  * @param tdef a `TerminalDefinition` object, indicating the specific functionality of the resulting `Terminal`
  * @param id the globally unique identifier to which this `Terminal` will be registered
  */
class TerminalObjectBuilder(private val tdef : TerminalDefinition, private val id : Int) extends ServerObjectBuilder[Terminal] {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : Terminal = {
    val obj = Terminal(tdef)
    guid.register(obj, id) //non-Actor GUID registration
    obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), s"${tdef.Name}_${obj.GUID.guid}")
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
