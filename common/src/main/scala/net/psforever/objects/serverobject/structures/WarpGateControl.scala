// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorRef
import net.psforever.objects.{Ntu, NtuContainer, NtuStorageBehavior}

class WarpGateControl(gate : WarpGate) extends BuildingControl(gate)
  with NtuStorageBehavior {
  override def receive : Receive = storageBehavior.orElse(super.receive)

  /**
    * Warp gates don't need to respond to offers.
    */
  def HandleNtuOffer(sender : ActorRef, src : NtuContainer) : Unit = {}

  /**
    * Warp gates don't need to stop.
    */
  def StopNtuBehavior(sender : ActorRef) : Unit = {}

  /**
    * When processing a request, the only important consideration is whether the warp gate is active.
    * @param sender na
    * @param min a minimum amount of nanites requested;
    * @param max the amount of nanites required to not make further requests;
    */
  def HandleNtuRequest(sender : ActorRef, min : Int, max : Int) : Unit = {
    sender ! Ntu.Grant(gate, if (gate.Active) min else 0)
  }

  /**
    * Warp gates doesn't need additional nanites.
    * For the sake of not letting any go to waste, it will give back those nanites for free.
    */
  def HandleNtuGrant(sender : ActorRef, src : NtuContainer, amount : Int) : Unit = {
    sender ! Ntu.Grant(gate, amount)
  }
}
