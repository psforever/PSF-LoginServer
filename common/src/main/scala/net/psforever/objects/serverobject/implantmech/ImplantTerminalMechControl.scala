// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import akka.actor.Actor
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.HackableBehavior

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech : ImplantTerminalMech) extends Actor with FactionAffinityBehavior.Check
  with MountableBehavior.Mount with MountableBehavior.Dismount with HackableBehavior.GenericHackable {
  def MountableObject = mech //do not add type!
  def HackableObject = mech

  def FactionObject : FactionAffinity = mech

  def receive : Receive = checkBehavior
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse(hackableBehavior)
    .orElse {
      case _ => ;
    }
}
