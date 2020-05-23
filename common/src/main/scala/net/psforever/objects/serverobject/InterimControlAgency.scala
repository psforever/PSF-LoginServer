 // Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject

import akka.actor.{Actor, ActorRef, Stash}

class InterimControlAgency extends Actor
  with Stash {
  private var heir : ActorRef = context.system.deadLetters

  def receive : Receive = {
    case InterimControlAgency.ControlAgency(ActorRef.noSender) => ;
    case InterimControlAgency.ControlAgency(actor) =>
      heir = actor
      context.become(Inheritance)
      unstashAll() //restore stashed messages to (front of) mailbox
      self ! akka.actor.PoisonPill //last message
    case _ =>
      stash()
  }

  def Inheritance : Receive = {
    case msg => heir forward msg
  }
}

object InterimControlAgency {
  final case class ControlAgency(actor : ActorRef)
}
