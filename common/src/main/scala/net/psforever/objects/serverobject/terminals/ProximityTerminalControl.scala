// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `ProximityTerminal`.
  * Although this "terminal" itself does not accept the same messages as a normal `Terminal` object,
  * it returns the same type of messages - wrapped in a `TerminalMessage` - to the `sender`.
  * @param term the proximity unit (terminal)
  */
class ProximityTerminalControl(term : Terminal with ProximityUnit) extends Actor with FactionAffinityBehavior.Check with ProximityUnit.Use {
  def FactionObject : FactionAffinity = term

  def TerminalObject : Terminal with ProximityUnit = term

  def receive : Receive = checkBehavior
    .orElse(proximityBehavior)
    .orElse {
      case _ => ;
    }

  override def toString : String = term.Definition.Name
}
