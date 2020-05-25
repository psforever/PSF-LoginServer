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
  import akka.actor.ActorRef
  private var defaultRef : ActorRef = ActorRef.noSender
  /**
    * Set the default actor.
    * It's recommended to set this to something else at the start of the akka universe.
    * `system.deadLetters` is a decent recommendation
    * since it will exist for the duration of the said akka universe and is also a facet of it.
    * @param ref the new default actor
    * @return the new default actor
    */
  def apply(ref : ActorRef) : ActorRef = {
    defaultRef = ref
    ref
  }

  final def Actor : ActorRef = defaultRef
}
