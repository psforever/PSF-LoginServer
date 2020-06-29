// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.vehicles.Ntu

class WarpGateControl(gate : WarpGate) extends BuildingControl(gate) {
  override def receive : Receive = warpGateReceive.orElse(super.receive)

  def warpGateReceive : Receive = {
    case Ntu.Request(_, 0) => ;

    case Ntu.Request(_, _) =>
      sender ! (if(gate.Active) {
        Ntu.Grant(100) //an active warp gates can always grant ntu
      }
      else {
        Ntu.Grant(0)
      })
  }
}
