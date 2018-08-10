// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}


class CaptureTerminalControl(terminal : CaptureTerminal) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = terminal

  def receive : Receive = checkBehavior.orElse {
    case CommonMessages.Hack(player) =>
      terminal.HackedBy = player
      sender ! true
    case CommonMessages.ClearHack() =>
      terminal.HackedBy = None

    case _ => ; //no default message
  }
}
