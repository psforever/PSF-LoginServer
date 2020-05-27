// Copyright (c) 2017 PSForever
package net.psforever.objects

object Default {
  //cancellable
  //TODO change all Default.Cancellable with Default.Cancellable
  import akka.actor.Cancellable
  protected class InternalCancellable extends Cancellable {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }
  private val cancellable : Cancellable = new InternalCancellable

  /**
    * Used to initialize the value of a re-usable `Cancellable` object.
    * By convention, it always acts like it has been cancelled before and can be cancelled.
    * Should be replaced with pertinent `Cancellable` logic through the initialization of an executor.
    */
  final def Cancellable : Cancellable = cancellable

  //actor
  import akka.actor.{Actor => AkkaActor, ActorRef, ActorSystem, Props}
  /**
    * An actor designed to wrap around `deadLetters` and redirect all normal messages.
    */
  private class DefaultActor extends AkkaActor {
    def receive : Receive = {
      case msg => context.system.deadLetters forward msg
    }
  }
  private var defaultRef : ActorRef = ActorRef.noSender
  /**
    * Instigate the default actor.
    * @param sys the actor universe under which this default actor will exist
    * @return the new default actor
    */
  def apply(sys : ActorSystem) : ActorRef = {
    defaultRef = sys.actorOf(Props[DefaultActor], "system-default-actor")
    defaultRef
  }

  final def Actor : ActorRef = defaultRef
}
