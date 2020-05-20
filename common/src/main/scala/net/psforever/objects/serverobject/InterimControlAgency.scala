 // Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject

import akka.actor.{Actor, ActorRef, PoisonPill, Stash}

class InterimControlAgency extends Actor with Stash {
  private var heir : ActorRef = ActorRef.noSender

  def receive : Receive = Interim

  def Interim : Receive = {
    case InterimControlAgency.ControlAgency(ActorRef.noSender) => ;
    case InterimControlAgency.ControlAgency(actor) =>
      heir = actor
      context.become(Inheritance)
      unstashAll() //restore stashed messages to (front of) mailbox
      self ! InterimControlAgency.Interim() //last message
    case _ =>
      stash()
  }

  def Inheritance : Receive = {
    case InterimControlAgency.Interim() => context.become(Interim)
    case msg => heir forward msg
  }
}

object InterimControlAgency {
  final case class ControlAgency(actor : ActorRef)

  private case class Interim()

}
