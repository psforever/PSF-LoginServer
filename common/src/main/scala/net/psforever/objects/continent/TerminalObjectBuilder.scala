// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import net.psforever.objects.terminals.{Terminal, TerminalDefinition}

class TerminalObjectBuilder(tdef : TerminalDefinition, id : Int) extends ServerObjectBuilder {
  import akka.actor.ActorContext
  import net.psforever.objects.guid.NumberPoolHub

  def Build(implicit context : ActorContext, guid : NumberPoolHub) : Terminal = {
    val obj = Terminal(tdef)
    guid.register(obj, id)
    obj.Actor
    obj
  }
}

object TerminalObjectBuilder {
  def apply(tdef : TerminalDefinition, id : Int) : TerminalObjectBuilder = {
    new TerminalObjectBuilder(tdef, id)
  }
}
