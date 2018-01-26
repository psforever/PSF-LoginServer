// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import akka.actor.Actor
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech : ImplantTerminalMech) extends Actor with FactionAffinityBehavior.Check
  with MountableBehavior.Mount with MountableBehavior.Dismount {
  def MountableObject = mech //do not add type!

  def FactionObject : FactionAffinity = mech

  def receive : Receive = checkBehavior
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse {
      case _ => ;
    }
}
