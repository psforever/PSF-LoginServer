// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import akka.actor.Actor
import net.psforever.objects.mount.MountableBehavior

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech : ImplantTerminalMech) extends Actor with MountableBehavior {
  override def MountableObject = mech

  def receive : Receive = mountableBehavior.orElse {
    case _ => ;
  }
}
