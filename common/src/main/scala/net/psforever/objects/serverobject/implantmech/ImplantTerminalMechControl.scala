// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import net.psforever.objects.mount.MountableControl

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech : ImplantTerminalMech) extends MountableControl(mech) {
  override def receive : Receive = super[MountableControl].receive.orElse {
    case _ => ;
  }
}
