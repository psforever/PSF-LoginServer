// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior


class CaptureTerminalControl(terminal : CaptureTerminal) extends Actor with FactionAffinityBehavior.Check with HackableBehavior.GenericHackable {
  def FactionObject : FactionAffinity = terminal
  def HackableObject = terminal

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse {
    case _ => ; //no default message
  }
}
