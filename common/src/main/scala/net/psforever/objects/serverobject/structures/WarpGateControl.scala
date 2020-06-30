// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorRef
import net.psforever.objects.{Ntu, NtuStorageBehavior}

class WarpGateControl(gate : WarpGate) extends BuildingControl(gate)
  with NtuStorageBehavior {
  override def receive : Receive = storageBehavior.orElse(super.receive)

  def HandleNtuOffer(sender : ActorRef) : Unit = {}

  def StopNtuBehavior(sender : ActorRef) : Unit = {}

  def HandleNtuRequest(sender : ActorRef, min : Int, max : Int) : Unit = {
    sender ! (if(gate.Active) {
      Ntu.Grant(100) //an active warp gates can always grant ntu
    }
    else {
      Ntu.Grant(0)
    })
  }

  def HandleNtuGrant(sender : ActorRef, amount : Int) : Unit = {}
}
